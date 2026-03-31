package com.gdavidpb.tuindice.data.source.credentials

import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import eu.anifantakis.lib.ksafe.KSafe

class CredentialsDataSource(
	private val kSafe: KSafe
) : CredentialsRepository {
	override suspend fun hasPassword(): Boolean {
		return kSafe.getDirect<String?>(
			key = SecureStoreKeys.UNIVERSITY_PASSWORD,
			defaultValue = null
		)?.isNotBlank() == true
	}

	override suspend fun getPassword(): String {
		return kSafe.getDirect<String?>(
			key = SecureStoreKeys.UNIVERSITY_PASSWORD,
			defaultValue = null
		)?.takeIf { password ->
			password.isNotBlank()
		} ?: throw IllegalStateException("university password is not available")
	}

	override suspend fun setPassword(password: String) {
		kSafe.putDirect(
			key = SecureStoreKeys.UNIVERSITY_PASSWORD,
			value = password
		)
	}

	override suspend fun clearPassword() {
		kSafe.putDirect(
			key = SecureStoreKeys.UNIVERSITY_PASSWORD,
			value = ""
		)
	}

	private object SecureStoreKeys {
		const val UNIVERSITY_PASSWORD = "universityPassword"
	}
}
