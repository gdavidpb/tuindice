package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.data.repository.MemorySessionDataRepository

class InMemorySessionDataSource : MemorySessionDataRepository {
	private var sessionId: String? = null
	private var accessToken: String? = null
	private var refreshToken: String? = null

	override suspend fun hasActiveSession(): Boolean {
		return sessionId != null && accessToken != null && refreshToken != null
	}

	override suspend fun setSessionId(sessionId: String) {
		this.sessionId = sessionId
	}

	override suspend fun setAccessToken(accessToken: String) {
		this.accessToken = accessToken
	}

	override suspend fun setRefreshToken(refreshToken: String) {
		this.refreshToken = refreshToken
	}

	override suspend fun getSessionId(): String? {
		return sessionId
	}

	override suspend fun getAccessToken(): String? {
		return accessToken
	}

	override suspend fun getRefreshToken(): String? {
		return refreshToken
	}

	override suspend fun clear() {
		sessionId = null
		accessToken = null
		refreshToken = null
	}
}
