package com.gdavidpb.tuindice.data.source.credentials

import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory

class CredentialsDataSource(
	private val secureStore: SecureKeyValueDataRepository,
	private val legacySecureStore: SecureKeyValueDataRepository
) : CredentialsRepository, SessionMemory {
	private var memoryPassword: String? = null

	// A store that fails to read is not a store without a password: the failure is thrown so
	// a caller that latches on "absent" never latches on a transient Keystore/Keychain error.
	override suspend fun hasPassword(): Boolean {
		return readPassword(strict = true) != null
	}

	override suspend fun getPassword(): String {
		return readPassword()
			?: throw IllegalStateException("university password is not available")
	}

	override suspend fun setPassword(password: String) {
		secureStore.putString(
			key = SecureStoreKeys.UNIVERSITY_PASSWORD,
			value = password
		)
		memoryPassword = password
		runCatching {
			legacySecureStore.remove(SecureStoreKeys.UNIVERSITY_PASSWORD)
		}
	}

	override suspend fun clearPassword() {
		memoryPassword = null
		runCatching {
			secureStore.remove(SecureStoreKeys.UNIVERSITY_PASSWORD)
		}
		runCatching {
			legacySecureStore.remove(SecureStoreKeys.UNIVERSITY_PASSWORD)
		}
	}

	// Only the copy held here: the stored one is wiped by the caller, and a later read falls
	// back to the store, so nothing of the signed-out account can be served from memory.
	override suspend fun clearSessionMemory() {
		memoryPassword = null
	}

	private suspend fun readPassword(strict: Boolean = false): String? {
		return memoryPassword
			?: readActivePassword(strict)?.also { password -> memoryPassword = password }
			?: readLegacyPassword()?.let { password -> migrateLegacyPassword(password) }
	}

	private suspend fun readActivePassword(strict: Boolean): String? {
		if (strict) {
			return secureStore.getString(SecureStoreKeys.UNIVERSITY_PASSWORD)
				?.takeIf(String::isNotBlank)
		}

		return runCatching {
			secureStore.getString(SecureStoreKeys.UNIVERSITY_PASSWORD)
				?.takeIf(String::isNotBlank)
		}.getOrNull()
	}

	private suspend fun readLegacyPassword(): String? {
		return runCatching {
			legacySecureStore.getString(SecureStoreKeys.UNIVERSITY_PASSWORD)
				?.takeIf(String::isNotBlank)
		}.getOrNull()
	}

	private suspend fun migrateLegacyPassword(password: String): String? {
		return runCatching {
			secureStore.putString(
				key = SecureStoreKeys.UNIVERSITY_PASSWORD,
				value = password
			)
			check(
				secureStore.getString(SecureStoreKeys.UNIVERSITY_PASSWORD) == password
			)
			legacySecureStore.remove(SecureStoreKeys.UNIVERSITY_PASSWORD)
			memoryPassword = password
			password
		}.getOrElse {
			runCatching {
				secureStore.remove(SecureStoreKeys.UNIVERSITY_PASSWORD)
			}
			null
		}
	}

	private object SecureStoreKeys {
		const val UNIVERSITY_PASSWORD = "universityPassword"
	}
}
