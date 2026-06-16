package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot

interface SessionRecoveryRepository {
	suspend fun recoverUnauthorizedSession(
		attemptedAuthorizationAccessToken: String?,
		attemptedCachedAccessToken: String?,
		attemptedCachedRefreshToken: String?
	): SessionSnapshot?

	suspend fun invalidateSession(sessionId: String? = null)
}
