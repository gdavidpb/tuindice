package com.gdavidpb.tuindice.platform.android

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.gdavidpb.tuindice.base.data.model.AttestationProofOfPossessionRequest
import eu.anifantakis.lib.ksafe.KSafe
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.UUID
import kotlin.io.encoding.Base64

class AndroidKeystoreProofOfPossessionCapability(
	private val kSafe: KSafe
) : AndroidProofOfPossessionCapability {
	override suspend fun resolveProofOfPossessionKeyId(): String {
		val existingKeyId = storedKeyId()

		if (!existingKeyId.isNullOrBlank()) {
			return existingKeyId
		}

		val keyId = UUID.randomUUID().toString()
		generateKeyPair(keyId)
		storeKeyId(keyId)
		return keyId
	}

	override suspend fun invalidateProofOfPossessionKeyId() {
		val keyId = storedKeyId()?.takeIf { value -> value.isNotBlank() } ?: return

		runCatching {
			keyStore().deleteEntry(aliasFor(keyId))
		}
		storeKeyId("")
	}

	override suspend fun createProofOfPossession(
		attestationInput: String,
		keyId: String
	): AttestationProofOfPossessionRequest {
		val entry = keyStore()
			.getEntry(aliasFor(keyId), null) as? KeyStore.PrivateKeyEntry
			?: throw IllegalStateException("Android Keystore entry not found for attestation key.")
		val signature = Signature.getInstance(SIGNATURE_ALGORITHM).apply {
			initSign(entry.privateKey)
			update(attestationInput.encodeToByteArray())
		}.sign()

		return AttestationProofOfPossessionRequest(
			signature = encodeBase64Url(signature),
			publicKey = encodeBase64Url(entry.certificate.publicKey.encoded)
		)
	}

	private fun generateKeyPair(keyId: String) {
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
			.build()

		generator.initialize(spec)
		generator.generateKeyPair()
	}

	private fun keyStore(): KeyStore {
		return KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER).apply {
			load(null)
		}
	}

	private fun storedKeyId(): String? {
		return kSafe.getDirect<String?>(
			key = SecureStoreKeys.ATTESTATION_KEY_ID,
			defaultValue = null
		)
	}

	private fun storeKeyId(keyId: String) {
		kSafe.putDirect(
			key = SecureStoreKeys.ATTESTATION_KEY_ID,
			value = keyId
		)
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
