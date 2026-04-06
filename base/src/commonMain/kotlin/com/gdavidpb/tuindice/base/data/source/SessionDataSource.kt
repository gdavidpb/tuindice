package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.data.repository.MemorySessionDataRepository
import com.gdavidpb.tuindice.base.data.repository.PreferencesSessionDataRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository

class SessionDataSource(
	private val memorySessionDataSource: MemorySessionDataRepository,
	private val preferencesSessionDataSource: PreferencesSessionDataRepository
) : SessionRepository {
	override suspend fun hasActiveSession(): Boolean {
		return memorySessionDataSource.hasActiveSession() ||
				preferencesSessionDataSource.hasActiveSession()
	}

	override suspend fun setUsbId(usbId: String) {
		preferencesSessionDataSource.setUsbId(usbId)
	}

	override suspend fun setSessionId(sessionId: String) {
		memorySessionDataSource.setSessionId(sessionId)
		preferencesSessionDataSource.setSessionId(sessionId)
	}

	override suspend fun setAccessToken(accessToken: String) {
		memorySessionDataSource.setAccessToken(accessToken)
		preferencesSessionDataSource.setAccessToken(accessToken)
	}

	override suspend fun setRefreshToken(refreshToken: String) {
		memorySessionDataSource.setRefreshToken(refreshToken)
		preferencesSessionDataSource.setRefreshToken(refreshToken)
	}

	override suspend fun getUsbId(): String {
		return preferencesSessionDataSource.getUsbId() ?: throw IllegalStateException()
	}

	override suspend fun getSessionId(): String {
		val memorySessionId = memorySessionDataSource.getSessionId()

		if (memorySessionId != null) return memorySessionId

		val preferencesSessionId = preferencesSessionDataSource.getSessionId()

		if (preferencesSessionId != null) {
			memorySessionDataSource.setSessionId(preferencesSessionId)

			return preferencesSessionId
		}

		throw IllegalStateException()
	}

	override suspend fun getAccessToken(): String {
		val memoryAccessToken = memorySessionDataSource.getAccessToken()

		if (memoryAccessToken != null) return memoryAccessToken

		val preferencesAccessToken = preferencesSessionDataSource.getAccessToken()

		if (preferencesAccessToken != null) {
			memorySessionDataSource.setAccessToken(preferencesAccessToken)

			return preferencesAccessToken
		}

		throw IllegalStateException()
	}

	override suspend fun getRefreshToken(): String {
		val memoryRefreshToken = memorySessionDataSource.getRefreshToken()

		if (memoryRefreshToken != null) return memoryRefreshToken

		val preferencesRefreshToken = preferencesSessionDataSource.getRefreshToken()

		if (preferencesRefreshToken != null) {
			memorySessionDataSource.setRefreshToken(preferencesRefreshToken)

			return preferencesRefreshToken
		}

		throw IllegalStateException()
	}

	override suspend fun clear() {
		memorySessionDataSource.clear()
		preferencesSessionDataSource.clear()
	}
}
