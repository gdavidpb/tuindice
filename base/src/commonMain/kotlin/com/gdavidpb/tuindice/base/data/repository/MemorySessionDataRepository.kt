package com.gdavidpb.tuindice.base.data.repository

interface MemorySessionDataRepository {
	suspend fun hasActiveSession(): Boolean

	suspend fun setAccessToken(accessToken: String)
	suspend fun setRefreshToken(refreshToken: String)

	suspend fun getAccessToken(): String?
	suspend fun getRefreshToken(): String?

	suspend fun clear()
}