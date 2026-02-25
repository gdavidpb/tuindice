package com.gdavidpb.tuindice.data.source.securestore

import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import com.gdavidpb.tuindice.base.data.source.SecureStoreDataSource
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AndroidSecureStoreDataSource(
	private val sharedPreferences: SharedPreferences
) : SecureStoreDataSource {
	override fun contains(key: String): Boolean {
		return getString(key) != null
	}

	override fun getString(key: String): String? {
		val payload = sharedPreferences.getString(key, null) ?: return null

		return if (payload.startsWith(ENCRYPTED_PREFIX)) {
			runCatching { decrypt(payload) }
				.onFailure {
					sharedPreferences.edit { remove(key) }
				}
				.getOrNull()
		} else {
			// Reject unexpected plaintext payloads for greenfield installs.
			sharedPreferences.edit { remove(key) }
			null
		}
	}

	override fun putString(key: String, value: String) {
		val encryptedPayload = encrypt(value)

		sharedPreferences.edit {
			putString(key, encryptedPayload)
		}
	}

	override fun clear() {
		sharedPreferences.edit {
			clear()
		}
	}

	private fun encrypt(value: String): String {
		val cipher = Cipher.getInstance(TRANSFORMATION)

		cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())

		val encrypted = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
		val iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
		val bytes = Base64.encodeToString(encrypted, Base64.NO_WRAP)

		return "$ENCRYPTED_PREFIX$iv:$bytes"
	}

	private fun decrypt(payload: String): String {
		val value = payload.removePrefix(ENCRYPTED_PREFIX)
		val payloadParts = value.split(":", limit = 2)

		require(payloadParts.size == 2) { "Invalid encrypted payload format." }

		val iv = Base64.decode(payloadParts[0], Base64.NO_WRAP)
		val encrypted = Base64.decode(payloadParts[1], Base64.NO_WRAP)
		val cipher = Cipher.getInstance(TRANSFORMATION)

		cipher.init(
			Cipher.DECRYPT_MODE,
			getOrCreateSecretKey(),
			GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
		)

		return String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)
	}

	private fun getOrCreateSecretKey(): SecretKey {
		val keyStore = KeyStore.getInstance(KEY_STORE_PROVIDER).apply {
			load(null)
		}

		val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey

		if (existingKey != null)
			return existingKey

		val keyGenerator = KeyGenerator.getInstance(
			KeyProperties.KEY_ALGORITHM_AES,
			KEY_STORE_PROVIDER
		)

		val parameterSpec = KeyGenParameterSpec.Builder(
			KEY_ALIAS,
			KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
		)
			.setBlockModes(KeyProperties.BLOCK_MODE_GCM)
			.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
			.setRandomizedEncryptionRequired(true)
			.build()

		keyGenerator.init(parameterSpec)

		return keyGenerator.generateKey()
	}

	companion object {
		private const val KEY_ALIAS = "tuindice_secure_store_v1"
		private const val KEY_STORE_PROVIDER = "AndroidKeyStore"
		private const val TRANSFORMATION = "AES/GCM/NoPadding"
		private const val GCM_TAG_LENGTH_BITS = 128
		private const val ENCRYPTED_PREFIX = "v1:"
	}
}
