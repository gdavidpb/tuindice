package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.data.repository.MemorySessionDataRepository

class InMemorySessionDataSource : MemorySessionDataRepository {
	private var accessToken: String? = null
	private var refreshToken: String? = null

	override suspend fun hasActiveSession(): Boolean {
		return accessToken != null || refreshToken != null
	}

	override suspend fun setAccessToken(accessToken: String) {
		this.accessToken = accessToken
	}

	override suspend fun setRefreshToken(refreshToken: String) {
		this.refreshToken = refreshToken
	}

	override suspend fun getAccessToken(): String? {
		return accessToken
	}

	override suspend fun getRefreshToken(): String? {
		return refreshToken
	}

	override suspend fun clear() {
		accessToken = null
		refreshToken = null
	}
}
