package com.gdavidpb.tuindice.login.data.repository.login

import com.gdavidpb.tuindice.base.domain.model.Attestation

interface RemoteDataSource {
	suspend fun auth(
		username: String,
		password: String,
		attestation: Attestation
	): String
}