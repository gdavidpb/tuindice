package com.gdavidpb.tuindice.login.data.repository.login

import com.gdavidpb.tuindice.login.domain.model.SignIn
import com.gdavidpb.tuindice.login.domain.repository.SignInRepository

class SignInDataRepository(
	private val remoteDataSource: RemoteDataSource
) : SignInRepository {
	override suspend fun signIn(
		username: String,
		password: String,
		attestation: String
	): SignIn {
		return remoteDataSource.signIn(username, password, attestation)
	}
}