package com.gdavidpb.tuindice.security.domain.model

sealed interface AttestationAuthorization {
	data class Bearer(
		val accessToken: String
	) : AttestationAuthorization

	data class Session(
		val sessionId: String,
		val refreshToken: String
	) : AttestationAuthorization

	data object CurrentSession : AttestationAuthorization
}
