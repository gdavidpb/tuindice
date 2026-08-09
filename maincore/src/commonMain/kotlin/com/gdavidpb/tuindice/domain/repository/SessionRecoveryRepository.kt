package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot

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

	suspend fun invalidateSession(sessionId: String? = null)
}
