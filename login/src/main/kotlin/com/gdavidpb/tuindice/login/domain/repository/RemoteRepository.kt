package com.gdavidpb.tuindice.login.domain.repository

import com.gdavidpb.tuindice.login.domain.model.SignIn

interface RemoteRepository {
	suspend fun signIn(
		username: String,
		password: String,
		messagingToken: String? = null,
		refreshToken: Boolean
	): SignIn
}