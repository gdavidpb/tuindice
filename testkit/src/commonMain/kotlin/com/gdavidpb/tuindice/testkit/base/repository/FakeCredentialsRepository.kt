package com.gdavidpb.tuindice.testkit.base.repository

import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository

class FakeCredentialsRepository(
	password: String? = null
) : CredentialsRepository {
	var password: String? = password
	val storedPasswords = mutableListOf<String>()
	var clearCalls = 0
		private set

	override suspend fun hasPassword(): Boolean = password != null

	override suspend fun getPassword(): String = password ?: throw IllegalStateException()

	override suspend fun setPassword(password: String) {
		this.password = password
		storedPasswords += password
	}

	override suspend fun clearPassword() {
		password = null
		clearCalls++
	}
}
