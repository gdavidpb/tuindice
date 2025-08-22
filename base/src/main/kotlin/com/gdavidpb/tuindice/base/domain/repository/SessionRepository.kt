package com.gdavidpb.tuindice.base.domain.repository

interface SessionRepository {
	suspend fun hasActiveSession(): Boolean

	suspend fun setUsbId(usbId: String)
	suspend fun setAccessToken(accessToken: String)
	suspend fun setRefreshToken(refreshToken: String)

	suspend fun getUsbId(): String
	suspend fun getAccessToken(): String
	suspend fun getRefreshToken(): String

	suspend fun clear()
}