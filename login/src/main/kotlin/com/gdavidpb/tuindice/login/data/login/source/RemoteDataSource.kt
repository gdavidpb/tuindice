package com.gdavidpb.tuindice.login.data.login.source

import com.gdavidpb.tuindice.login.domain.model.SignIn

interface RemoteDataSource {
	suspend fun signIn(
		username: String,
		password: String,
		refreshToken: Boolean
	): SignIn
}