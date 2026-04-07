package com.gdavidpb.tuindice.auth.domain.exception

enum class AuthenticationStage {
	SignInBootstrap,
	SignInAttestation,
	SignInExchange,
	UpdatePasswordAttestation,
	UpdatePasswordReissue
}

class AuthenticationStageException(
	val stage: AuthenticationStage,
	cause: Throwable
) : RuntimeException(cause)
