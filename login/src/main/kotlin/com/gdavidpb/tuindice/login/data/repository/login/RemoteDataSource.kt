package com.gdavidpb.tuindice.login.data.repository.login

import com.gdavidpb.tuindice.login.domain.model.SignIn

interface RemoteDataSource {
	suspend fun signIn(
		username: String,
		password: String
	): SignIn
}