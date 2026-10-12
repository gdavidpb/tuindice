package com.gdavidpb.tuindice.base.data.repository


interface PreferencesSessionDataRepository {
	suspend fun setUsbId(usbId: String)
	suspend fun setSessionId(sessionId: String)
	suspend fun setAccessToken(accessToken: String)
	suspend fun setRefreshToken(refreshToken: String)
	suspend fun setSession(usbId: String, sessionId: String, accessToken: String, refreshToken: String)

	suspend fun getUsbId(): String?
	suspend fun getSessionId(): String?
	suspend fun getAccessToken(): String?
	suspend fun getRefreshToken(): String?

	suspend fun clear()
}
