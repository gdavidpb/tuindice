package com.gdavidpb.tuindice.login.data.repository.login

import com.gdavidpb.tuindice.login.domain.repository.SignInRepository

class SignInDataRepository(
	private val remoteDataSource: RemoteDataSource
) : SignInRepository {
	override suspend fun auth(
		username: String,
		password: String,
		attestation: String
	): String {
		return remoteDataSource.auth(username, password, attestation)
	}
}