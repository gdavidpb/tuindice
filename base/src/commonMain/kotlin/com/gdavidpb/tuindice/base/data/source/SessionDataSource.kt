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

	override suspend fun getUsbId(): String {
		return sessionMutex.withLock {
			memorySessionDataSource.getUsbId()
				?: preferencesSessionDataSource.getUsbId()?.also { usbId ->
					memorySessionDataSource.setUsbId(usbId)
				}
				?: throw IllegalStateException()
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
		val memoryUsbId = memorySessionDataSource.getUsbId()
		val memorySessionId = memorySessionDataSource.getSessionId()
		val memoryAccessToken = memorySessionDataSource.getAccessToken()
		val memoryRefreshToken = memorySessionDataSource.getRefreshToken()

		if (
			memoryUsbId != null &&
			memorySessionId != null &&
			memoryAccessToken != null &&
			memoryRefreshToken != null
		) {
			return SessionSnapshot(
				sessionId = memorySessionId,
				accessToken = memoryAccessToken,
				refreshToken = memoryRefreshToken,
				usbId = memoryUsbId
			)
		}

		val usbId = preferencesSessionDataSource.getUsbId()
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

		memorySessionDataSource.setUsbId(usbId)
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

	// Memory first, then the store. Every access goes through the session lock and the four keys of the
	// store are written without suspending, so a cancellation cannot leave the store half written: the
	// save is whole or it never starts. Only a failed write of the store leaves the memory with the new
	// tokens and the store with the old ones. The process goes on with the memory and the next refresh
	// writes the store again. If the process dies before that, the old refresh token is still accepted
	// during the grace window of the server, and after it the server answers `refresh_token_mismatch`,
	// which ends the session with a notice: there is no sign-in by credentials in that case.
	private suspend fun writeSessionSnapshot(snapshot: SessionSnapshot) {
		memorySessionDataSource.setUsbId(snapshot.usbId)
		memorySessionDataSource.setSessionId(snapshot.sessionId)
		memorySessionDataSource.setAccessToken(snapshot.accessToken)
		memorySessionDataSource.setRefreshToken(snapshot.refreshToken)
		preferencesSessionDataSource.setSession(
			usbId = snapshot.usbId,
			sessionId = snapshot.sessionId,
			accessToken = snapshot.accessToken,
			refreshToken = snapshot.refreshToken
		)
	}
}
