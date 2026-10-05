package com.gdavidpb.tuindice.base.domain.exception

/**
 * Thrown instead of calling the server while the wait it asked for (`Retry-After`, or the
 * backoff after consecutive 503s) has not elapsed. Everything that treats a 503 as "unavailable"
 * treats this the same way.
 */
class ServiceRetryWindowException(
	val retryAfterMillis: Long
) : RuntimeException("The service asked to wait ${retryAfterMillis}ms before retrying.")
