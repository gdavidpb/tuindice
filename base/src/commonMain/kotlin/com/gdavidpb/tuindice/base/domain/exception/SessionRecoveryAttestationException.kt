package com.gdavidpb.tuindice.base.domain.exception

/**
 * Attestation was refused with no auth verdict while a session was being recovered: an
 * integrity-plumbing failure, not a statement about the session or about the request that needed
 * it. Thrown instead of the raw rejection so callers can tell the two apart.
 */
class SessionRecoveryAttestationException(
	cause: Throwable
) : Exception("Attestation rejected during session recovery.", cause)
