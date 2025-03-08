package com.gdavidpb.tuindice.login.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Attestation

interface SignInRepository {
	suspend fun auth(
		username: String,
		password: String,
		attestation: Attestation
	): String
}