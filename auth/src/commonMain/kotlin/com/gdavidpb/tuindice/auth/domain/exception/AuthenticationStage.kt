package com.gdavidpb.tuindice.auth.domain.exception

enum class AuthenticationStage {
	SignInBootstrap,
	SignInAttestation,
	SignInExchange,
	UpdatePasswordAttestation,
	UpdatePasswordReissue
}
