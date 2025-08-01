package com.gdavidpb.tuindice.base.data.source

interface PreferencesSessionDataSource {
	suspend fun hasActiveSession(): Boolean

	suspend fun setAccessToken(accessToken: String)
	suspend fun setRefreshToken(refreshToken: String)

	suspend fun getAccessToken(): String?
	suspend fun getRefreshToken(): String?
}