package com.gdavidpb.tuindice.login.data.repository.login.source

import com.gdavidpb.tuindice.base.utils.extension.getOrThrow
import com.gdavidpb.tuindice.login.data.repository.login.RemoteDataSource
import com.gdavidpb.tuindice.login.data.repository.login.SignInApi
import com.gdavidpb.tuindice.login.data.repository.login.source.api.mapper.toSignIn
import com.gdavidpb.tuindice.login.domain.model.SignIn
import okhttp3.Credentials

class SignInApiDataSource(
	private val signInApi: SignInApi
) : RemoteDataSource {
	override suspend fun signIn(
		username: String,
		password: String
	): SignIn {
		return signInApi.signIn(
			basicToken = Credentials.basic(username, password)
		)
			.getOrThrow()
			.toSignIn()
	}
}