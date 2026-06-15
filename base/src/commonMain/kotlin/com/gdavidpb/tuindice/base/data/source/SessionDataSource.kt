package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.data.repository.MemorySessionDataRepository
import com.gdavidpb.tuindice.base.data.repository.PreferencesSessionDataRepository
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SessionDataSource(
	private val memorySessionDataSource: MemorySessionDataRepository,
	private val preferencesSessionDataSource: PreferencesSessionDataRepository
) : SessionRepository {
	private val sessionMutex = Mutex()

	override suspend fun hasActiveSession(): Boolean {
		return getActiveSessionSnapshot() != null
	}

	override suspend fun getActiveSessionSnapshot(): SessionSnapshot? {
		return sessionMutex.withLock {
			readActiveSessionSnapshot()
		}
	}

	override suspend fun setSessionSnapshot(snapshot: SessionSnapshot) {
		sessionMutex.withLock {
			writeSessionSnapshot(snapshot)
		}
	}

	override suspend fun replaceSessionSnapshotIfCurrent(
		expectedSnapshot: SessionSnapshot,
		newSnapshot: SessionSnapshot
	): Boolean {
		return sessionMutex.withLock {
			if (readActiveSessionSnapshot() != expectedSnapshot) return@withLock false

			writeSessionSnapshot(newSnapshot)
			true
		}
	}

	override suspend fun setUsbId(usbId: String) {
		sessionMutex.withLock {
			preferencesSessionDataSource.setUsbId(usbId)
		}
	}

	override suspend fun setSessionId(sessionId: String) {
		sessionMutex.withLock {
			memorySessionDataSource.setSessionId(sessionId)
			preferencesSessionDataSource.setSessionId(sessionId)
		}
	}

	override suspend fun setAccessToken(accessToken: String) {
		sessionMutex.withLock {
			memorySessionDataSource.setAccessToken(accessToken)
			preferencesSessionDataSource.setAccessToken(accessToken)
		}
	}

	override suspend fun setRefreshToken(refreshToken: String) {
		sessionMutex.withLock {
			memorySessionDataSource.setRefreshToken(refreshToken)
			preferencesSessionDataSource.setRefreshToken(refreshToken)
		}
	}

	override suspend fun getUsbId(): String {
		return sessionMutex.withLock {
			preferencesSessionDataSource.getUsbId() ?: throw IllegalStateException()
		}
	}

	override suspend fun getSessionId(): String {
		return sessionMutex.withLock {
			val memorySessionId = memorySessionDataSource.getSessionId()

			if (memorySessionId != null) return@withLock memorySessionId

			val preferencesSessionId = preferencesSessionDataSource.getSessionId()

			if (preferencesSessionId != null) {
				memorySessionDataSource.setSessionId(preferencesSessionId)

				return@withLock preferencesSessionId
			}

			throw IllegalStateException()
		}
	}

	override suspend fun getAccessToken(): String {
		return sessionMutex.withLock {
			val memoryAccessToken = memorySessionDataSource.getAccessToken()

			if (memoryAccessToken != null) return@withLock memoryAccessToken

			val preferencesAccessToken = preferencesSessionDataSource.getAccessToken()

			if (preferencesAccessToken != null) {
				memorySessionDataSource.setAccessToken(preferencesAccessToken)

				return@withLock preferencesAccessToken
			}

			throw IllegalStateException()
		}
	}

	override suspend fun getRefreshToken(): String {
		return sessionMutex.withLock {
			val memoryRefreshToken = memorySessionDataSource.getRefreshToken()

			if (memoryRefreshToken != null) return@withLock memoryRefreshToken

			val preferencesRefreshToken = preferencesSessionDataSource.getRefreshToken()

			if (preferencesRefreshToken != null) {
				memorySessionDataSource.setRefreshToken(preferencesRefreshToken)

				return@withLock preferencesRefreshToken
			}

			throw IllegalStateException()
		}
	}

	override suspend fun clear() {
		sessionMutex.withLock {
			memorySessionDataSource.clear()
			preferencesSessionDataSource.clear()
		}
	}

	private suspend fun readActiveSessionSnapshot(): SessionSnapshot? {
		val memorySessionId = memorySessionDataSource.getSessionId()
		val memoryAccessToken = memorySessionDataSource.getAccessToken()
		val memoryRefreshToken = memorySessionDataSource.getRefreshToken()
		val usbId = preferencesSessionDataSource.getUsbId()

		if (
			memorySessionId != null &&
			memoryAccessToken != null &&
			memoryRefreshToken != null &&
			usbId != null
		) {
			return SessionSnapshot(
				sessionId = memorySessionId,
				accessToken = memoryAccessToken,
				refreshToken = memoryRefreshToken,
				usbId = usbId
			)
		}

		val preferencesSessionId = preferencesSessionDataSource.getSessionId()
		val preferencesAccessToken = preferencesSessionDataSource.getAccessToken()
		val preferencesRefreshToken = preferencesSessionDataSource.getRefreshToken()

		if (
			preferencesSessionId == null ||
			preferencesAccessToken == null ||
			preferencesRefreshToken == null ||
			usbId == null
		) {
			return null
		}

		memorySessionDataSource.setSessionId(preferencesSessionId)
		memorySessionDataSource.setAccessToken(preferencesAccessToken)
		memorySessionDataSource.setRefreshToken(preferencesRefreshToken)

		return SessionSnapshot(
			sessionId = preferencesSessionId,
			accessToken = preferencesAccessToken,
			refreshToken = preferencesRefreshToken,
			usbId = usbId
		)
	}

	private suspend fun writeSessionSnapshot(snapshot: SessionSnapshot) {
		memorySessionDataSource.setSessionId(snapshot.sessionId)
		memorySessionDataSource.setAccessToken(snapshot.accessToken)
		memorySessionDataSource.setRefreshToken(snapshot.refreshToken)
		preferencesSessionDataSource.setSessionId(snapshot.sessionId)
		preferencesSessionDataSource.setAccessToken(snapshot.accessToken)
		preferencesSessionDataSource.setRefreshToken(snapshot.refreshToken)
		preferencesSessionDataSource.setUsbId(snapshot.usbId)
	}
}
