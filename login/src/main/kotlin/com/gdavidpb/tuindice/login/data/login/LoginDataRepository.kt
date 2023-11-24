package com.gdavidpb.tuindice.login.data.login

import com.gdavidpb.tuindice.login.data.login.source.RemoteDataSource
import com.gdavidpb.tuindice.login.domain.model.SignIn
import com.gdavidpb.tuindice.login.domain.repository.LoginRepository

class LoginDataRepository(
	private val remoteDataSource: RemoteDataSource
) : LoginRepository {
	override suspend fun signIn(
		username: String,
		password: String,
		refreshToken: Boolean
	): SignIn {
		return remoteDataSource.signIn(username, password, refreshToken)
	}
}