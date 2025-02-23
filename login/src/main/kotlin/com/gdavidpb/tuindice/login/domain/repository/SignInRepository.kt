package com.gdavidpb.tuindice.login.domain.repository

interface SignInRepository {
	suspend fun auth(
		username: String,
		password: String,
		attestation: String
	): String
}