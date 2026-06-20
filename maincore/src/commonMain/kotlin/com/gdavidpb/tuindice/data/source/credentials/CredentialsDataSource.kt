package com.gdavidpb.tuindice.data.source.credentials

import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository

class CredentialsDataSource(
	private val secureStore: SecureKeyValueDataRepository,
	private val legacySecureStore: SecureKeyValueDataRepository
) : CredentialsRepository {
	private var memoryPassword: String? = null

	override suspend fun hasPassword(): Boolean {
		return readPassword() != null
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

	private suspend fun readPassword(): String? {
		memoryPassword?.let { return it }

		val activePassword = runCatching {
			secureStore.getString(SecureStoreKeys.UNIVERSITY_PASSWORD)
				?.takeIf(String::isNotBlank)
		}.getOrNull()

		if (activePassword != null) {
			memoryPassword = activePassword
			return activePassword
		}

		val legacyPassword = runCatching {
			legacySecureStore.getString(SecureStoreKeys.UNIVERSITY_PASSWORD)
				?.takeIf(String::isNotBlank)
		}.getOrNull() ?: return null

		return migrateLegacyPassword(legacyPassword)
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
