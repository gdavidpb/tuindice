package com.gdavidpb.tuindice.auth.domain.exception

/**
 * [retryAfterMillis] is the wait the identity service asked for before this stage may be tried
 * again, resolved where the account is known; zero when it did not ask for one.
 */
class AuthenticationStageException(
	val stage: AuthenticationStage,
	cause: Throwable,
	val retryAfterMillis: Long = 0L
) : RuntimeException(cause)
