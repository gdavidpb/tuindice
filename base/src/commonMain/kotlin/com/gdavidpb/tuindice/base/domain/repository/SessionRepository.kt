package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot

interface SessionRepository {
	suspend fun hasActiveSession(): Boolean

	suspend fun getActiveSessionSnapshot(): SessionSnapshot?
	suspend fun setSessionSnapshot(snapshot: SessionSnapshot)
	suspend fun replaceSessionSnapshotIfCurrent(
		expectedSnapshot: SessionSnapshot,
		newSnapshot: SessionSnapshot
	): Boolean

	suspend fun setUsbId(usbId: String)
	suspend fun setSessionId(sessionId: String)
	suspend fun setAccessToken(accessToken: String)
	suspend fun setRefreshToken(refreshToken: String)

	suspend fun getUsbId(): String
	suspend fun getSessionId(): String
	suspend fun getAccessToken(): String
	suspend fun getRefreshToken(): String

	suspend fun clear()
}
