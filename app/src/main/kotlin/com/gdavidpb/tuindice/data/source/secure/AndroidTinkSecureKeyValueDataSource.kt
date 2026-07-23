package com.gdavidpb.tuindice.data.source.secure

import android.content.Context
import android.content.SharedPreferences
import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import com.gdavidpb.tuindice.base.domain.exception.SecureStoreUnavailableException
import com.google.crypto.tink.Aead
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import java.security.KeyStore
import java.util.Base64

class AndroidTinkSecureKeyValueDataSource : SecureKeyValueDataRepository {
	private val valuePreferences: SharedPreferences
	private val aeadProvider: AndroidSecureAeadProvider

	constructor(context: Context) : this(
		valuePreferences = context.applicationContext.getSharedPreferences(
			SECURE_VALUE_PREFS_NAME,
			Context.MODE_PRIVATE
		),
		aeadProvider = TinkAndroidSecureAeadProvider(context.applicationContext)
	)

	internal constructor(
		valuePreferences: SharedPreferences,
		aeadProvider: AndroidSecureAeadProvider
	) {
		this.valuePreferences = valuePreferences
		this.aeadProvider = aeadProvider
	}

	override suspend fun getString(key: String): String? {
		val encodedCiphertext = valuePreferences.getString(key, null) ?: return null

		return runCatching {
			val plaintext = availableAead(key).decrypt(
				decodeBase64(encodedCiphertext),
				associatedData(key)
			)
			plaintext.decodeToString()
		}.getOrElse { throwable ->
			if (throwable is SecureStoreUnavailableException) throw throwable

			throw IllegalStateException("Unable to read secure value for key $key.", throwable)
		}
	}

	override suspend fun putString(key: String, value: String) {
		val encodedCiphertext = runCatching {
			val ciphertext = availableAead(key).encrypt(
				value.encodeToByteArray(),
				associatedData(key)
			)
			encodeBase64(ciphertext)
		}.getOrElse { throwable ->
			if (throwable is SecureStoreUnavailableException) throw throwable

			throw IllegalStateException("Unable to encrypt secure value for key $key.", throwable)
		}

		check(
			valuePreferences.edit()
				.putString(key, encodedCiphertext)
				.commit()
		) {
			"Unable to persist secure value for key $key."
		}
	}

	override suspend fun remove(key: String) {
		check(
			valuePreferences.edit()
				.remove(key)
				.commit()
		) {
			"Unable to remove secure value for key $key."
		}
	}

	override suspend fun clear() {
		check(
			valuePreferences.edit()
				.clear()
				.commit()
		) {
			"Unable to clear secure values."
		}
		aeadProvider.clear()
	}

	// Failing to obtain the Aead is a keystore outage (recoverable); a decrypt
	// failure on existing ciphertext is corruption and keeps its old semantics.
	private fun availableAead(key: String): Aead {
		return runCatching { aeadProvider.aead() }.getOrElse { throwable ->
			throw SecureStoreUnavailableException("Secure store unavailable for key $key.", throwable)
		}
	}

	private fun associatedData(key: String): ByteArray {
		return "$ASSOCIATED_DATA_PREFIX$key".encodeToByteArray()
	}

	private fun encodeBase64(value: ByteArray): String {
		return Base64.getEncoder().encodeToString(value)
	}

	private fun decodeBase64(value: String): ByteArray {
		return Base64.getDecoder().decode(value)
	}

	private companion object {
		const val ASSOCIATED_DATA_PREFIX = "tuindice.secure_store.v1:"
		const val SECURE_VALUE_PREFS_NAME = "tuindice_secure_values"
	}
}

internal interface AndroidSecureAeadProvider {
	fun aead(): Aead
	fun clear()
}

private class TinkAndroidSecureAeadProvider(
	private val context: Context
) : AndroidSecureAeadProvider {
	private val lock = Any()
	private var cachedAead: Aead? = null

	override fun aead(): Aead {
		return synchronized(lock) {
			cachedAead ?: createAead().also { aead -> cachedAead = aead }
		}
	}

	override fun clear() {
		synchronized(lock) {
			cachedAead = null
			context.getSharedPreferences(KEYSET_PREFS_NAME, Context.MODE_PRIVATE)
				.edit()
				.clear()
				.commit()
			runCatching {
				KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER).apply {
					load(null)
					deleteEntry(MASTER_KEY_ALIAS)
				}
			}
		}
	}

	private fun createAead(): Aead {
		AeadConfig.register()
		val manager = AndroidKeysetManager.Builder()
			.withSharedPref(context, KEYSET_NAME, KEYSET_PREFS_NAME)
			.withKeyTemplate(AeadKeyTemplates.AES256_GCM)
			.withMasterKeyUri("$ANDROID_KEYSTORE_URI_PREFIX$MASTER_KEY_ALIAS")
			.build()

		return manager.keysetHandle.getPrimitive(
			RegistryConfiguration.get(),
			Aead::class.java
		)
	}

	private companion object {
		const val KEYSET_NAME = "tuindice_secure_store_keyset"
		const val KEYSET_PREFS_NAME = "tuindice_secure_keyset"
		const val MASTER_KEY_ALIAS = "tuindice_secure_store_master_key"
		const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"
		const val ANDROID_KEYSTORE_URI_PREFIX = "android-keystore://"
	}
}
