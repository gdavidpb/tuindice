package com.gdavidpb.tuindice.login.data.api

import com.gdavidpb.tuindice.base.utils.extension.getOrThrow
import com.gdavidpb.tuindice.login.data.api.mapper.toSignIn
import com.gdavidpb.tuindice.login.data.login.source.RemoteDataSource
import com.gdavidpb.tuindice.login.domain.model.SignIn
import okhttp3.Credentials

class ApiDataSource(
	private val signInApi: SignInApi
) : RemoteDataSource {
	override suspend fun signIn(
		username: String,
		password: String,
		refreshToken: Boolean
	): SignIn {
		return signInApi.signIn(
			basicToken = Credentials.basic(username, password),
			refreshToken = refreshToken
		)
			.getOrThrow()
			.toSignIn()
	}
}