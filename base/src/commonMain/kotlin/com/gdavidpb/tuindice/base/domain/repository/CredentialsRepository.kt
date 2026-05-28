package com.gdavidpb.tuindice.base.domain.repository

interface CredentialsRepository {
	suspend fun hasPassword(): Boolean
	suspend fun getPassword(): String
	suspend fun setPassword(password: String)
	suspend fun clearPassword()
}
