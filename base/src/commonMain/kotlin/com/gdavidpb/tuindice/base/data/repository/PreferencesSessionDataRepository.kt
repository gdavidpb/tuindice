package com.gdavidpb.tuindice.base.data.repository


interface PreferencesSessionDataRepository {
	suspend fun hasActiveSession(): Boolean

	suspend fun setSession(usbId: String, sessionId: String, accessToken: String, refreshToken: String)

	suspend fun getUsbId(): String?
	suspend fun getSessionId(): String?
	suspend fun getAccessToken(): String?
	suspend fun getRefreshToken(): String?

	suspend fun clear()
}
