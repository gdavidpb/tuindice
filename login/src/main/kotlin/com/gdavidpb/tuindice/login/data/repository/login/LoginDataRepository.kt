package com.gdavidpb.tuindice.login.data.repository.login

import com.gdavidpb.tuindice.login.domain.model.SignIn
import com.gdavidpb.tuindice.login.domain.repository.LoginRepository

class LoginDataRepository(
	private val remoteDataSource: RemoteDataSource
) : LoginRepository {
	override suspend fun signIn(
		username: String,
		password: String
	): SignIn {
		return remoteDataSource.signIn(username, password)
	}
}