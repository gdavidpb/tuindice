package com.gdavidpb.tuindice.testkit.base.repository

import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository

class FakeSessionRepository(
	private var sessionId: String = "session-123",
	private var usbId: String = "20261234",
	private var accessToken: String = "access-token",
	private var refreshToken: String = "refresh-token"
) : SessionRepository {
	var cleared = false
		private set

	override suspend fun hasActiveSession(): Boolean {
		return sessionId.isNotBlank() &&
				usbId.isNotBlank() &&
				accessToken.isNotBlank() &&
				refreshToken.isNotBlank()
	}

	override suspend fun getActiveSessionSnapshot(): SessionSnapshot? {
		return if (hasActiveSession()) {
			SessionSnapshot(
				sessionId = sessionId,
				accessToken = accessToken,
				refreshToken = refreshToken,
				usbId = usbId
			)
		} else {
			null
		}
	}

	override suspend fun setSessionSnapshot(snapshot: SessionSnapshot) {
		sessionId = snapshot.sessionId
		accessToken = snapshot.accessToken
		refreshToken = snapshot.refreshToken
		usbId = snapshot.usbId
	}

	override suspend fun replaceSessionSnapshotIfCurrent(
		expectedSnapshot: SessionSnapshot,
		newSnapshot: SessionSnapshot
	): Boolean {
		if (getActiveSessionSnapshot() != expectedSnapshot) return false

		setSessionSnapshot(newSnapshot)
		return true
	}

	override suspend fun getUsbId(): String = usbId

	override suspend fun getSessionId(): String = sessionId

	override suspend fun getAccessToken(): String = accessToken

	override suspend fun getRefreshToken(): String = refreshToken

	override suspend fun clear() {
		sessionId = ""
		usbId = ""
		accessToken = ""
		refreshToken = ""
		cleared = true
	}
}
