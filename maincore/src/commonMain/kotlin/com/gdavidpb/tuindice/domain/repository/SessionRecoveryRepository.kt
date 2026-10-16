package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.domain.model.SessionInvalidationReason

interface SessionRecoveryRepository {
	suspend fun recoverUnauthorizedSession(
		attemptedAuthorizationAccessToken: String?,
		attemptedCachedAccessToken: String?,
		attemptedCachedRefreshToken: String?
	): SessionSnapshot?

	/**
	 * Returns the freshest usable session snapshot, refreshing ahead of time when the
	 * access token is at or near expiry. Concurrent callers share a single refresh.
	 * When a proactive refresh cannot complete for non-session reasons, the stored
	 * snapshot is returned so the reactive 401 path stays the final authority.
	 */
	suspend fun ensureFreshSession(): SessionSnapshot?

	/**
	 * Ends the session: cancels the scope's work, wipes the device, tells the interface and keeps the
	 * notice for the next start. [reason] and [cause] only feed the report that each invalidation
	 * leaves behind.
	 */
	suspend fun invalidateSession(
		sessionId: String? = null,
		reason: SessionInvalidationReason? = null,
		cause: Throwable? = null
	)
}
