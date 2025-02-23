package com.gdavidpb.tuindice.login.data.repository.login

interface RemoteDataSource {
	suspend fun auth(
		username: String,
		password: String,
		attestation: String
	): String
}