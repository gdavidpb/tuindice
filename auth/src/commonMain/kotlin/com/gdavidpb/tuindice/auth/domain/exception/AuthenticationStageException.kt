package com.gdavidpb.tuindice.auth.domain.exception

class AuthenticationStageException(
	val stage: AuthenticationStage,
	cause: Throwable
) : RuntimeException(cause)
