package com.gdavidpb.tuindice.platform.android

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.gdavidpb.tuindice.security.data.model.AttestationProofOfPossessionRequest
import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.UUID
import kotlin.io.encoding.Base64

class AndroidKeystoreProofOfPossessionCapability(
	private val secureStore: SecureKeyValueDataRepository,
	private val legacySecureStore: SecureKeyValueDataRepository
) : AndroidProofOfPossessionCapability {
	override suspend fun resolveProofOfPossessionKeyId(): String {
		val existingKeyId = storedKeyId()

		if (!existingKeyId.isNullOrBlank()) {
			return existingKeyId
		}

		val keyId = UUID.randomUUID().toString()
		storeKeyId(keyId)
		return keyId
	}

	override suspend fun invalidateProofOfPossessionKeyId() {
		val keyId = storedKeyId()?.takeIf { value -> value.isNotBlank() } ?: return

		runCatching {
			keyStore().deleteEntry(aliasFor(keyId))
		}
		runCatching { secureStore.remove(SecureStoreKeys.ATTESTATION_KEY_ID) }
		runCatching { legacySecureStore.remove(SecureStoreKeys.ATTESTATION_KEY_ID) }
	}

	override suspend fun createProofOfPossession(
		attestationInput: String,
		keyId: String,
		requireKeyAttestation: Boolean
	): AttestationProofOfPossessionRequest {
		if (requireKeyAttestation) {
			runCatching {
				keyStore().deleteEntry(aliasFor(keyId))
			}
			generateKeyPair(
				keyId = keyId,
				attestationChallenge = attestationInput.encodeToByteArray()
			)
		}

		val entry = keyStore()
			.getEntry(aliasFor(keyId), null) as? KeyStore.PrivateKeyEntry
			?: throw IllegalStateException("Android Keystore entry not found for attestation key.")
		val signature = Signature.getInstance(SIGNATURE_ALGORITHM).apply {
			initSign(entry.privateKey)
			update(attestationInput.encodeToByteArray())
		}.sign()

		return AttestationProofOfPossessionRequest(
			signature = encodeBase64Url(signature),
			publicKey = encodeBase64Url(entry.certificate.publicKey.encoded),
			attestationCertificateChain = if (requireKeyAttestation) {
				(keyStore().getCertificateChain(aliasFor(keyId)) ?: emptyArray())
					.map { certificate -> encodeBase64Url(certificate.encoded) }
			} else {
				null
			}
		)
	}

	private fun generateKeyPair(
		keyId: String,
		attestationChallenge: ByteArray
	) {
		val generator = KeyPairGenerator.getInstance(
			KeyProperties.KEY_ALGORITHM_EC,
			ANDROID_KEYSTORE_PROVIDER
		)
		val spec = KeyGenParameterSpec.Builder(
			aliasFor(keyId),
			KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
		)
			.setDigests(KeyProperties.DIGEST_SHA256)
			.setAlgorithmParameterSpec(ECGenParameterSpec(EC_CURVE))
			.setAttestationChallenge(attestationChallenge)
			.build()

		generator.initialize(spec)
		generator.generateKeyPair()
	}

	private fun keyStore(): KeyStore {
		return KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER).apply {
			load(null)
		}
	}

	private suspend fun storedKeyId(): String? {
		val activeKeyId = runCatching {
			secureStore.getString(SecureStoreKeys.ATTESTATION_KEY_ID)
				?.takeIf(String::isNotBlank)
		}.getOrNull()

		if (activeKeyId != null) return activeKeyId

		val legacyKeyId = runCatching {
			legacySecureStore.getString(SecureStoreKeys.ATTESTATION_KEY_ID)
				?.takeIf(String::isNotBlank)
		}.getOrNull() ?: return null

		return migrateLegacyKeyId(legacyKeyId)
	}

	private suspend fun storeKeyId(keyId: String) {
		secureStore.putString(
			key = SecureStoreKeys.ATTESTATION_KEY_ID,
			value = keyId
		)
		runCatching { legacySecureStore.remove(SecureStoreKeys.ATTESTATION_KEY_ID) }
	}

	private suspend fun migrateLegacyKeyId(keyId: String): String? {
		return runCatching {
			storeKeyId(keyId)
			check(secureStore.getString(SecureStoreKeys.ATTESTATION_KEY_ID) == keyId)
			keyId
		}.getOrElse {
			runCatching { secureStore.remove(SecureStoreKeys.ATTESTATION_KEY_ID) }
			null
		}
	}

	private fun aliasFor(keyId: String): String {
		return "$KEY_ALIAS_PREFIX$keyId"
	}

	private fun encodeBase64Url(value: ByteArray): String {
		return Base64
			.UrlSafe
			.withPadding(Base64.PaddingOption.ABSENT)
			.encode(value)
	}

	private object SecureStoreKeys {
		const val ATTESTATION_KEY_ID = "attestationProofOfPossessionKeyId"
	}

	private companion object {
		const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"
		const val EC_CURVE = "secp256r1"
		const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
		const val KEY_ALIAS_PREFIX = "attestation-proof-"
	}
}
