package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.utils.PreferencesKeys

class SecureStoreSessionDataSource(
	private val secureStoreDataSource: SecureStoreDataSource
) : PreferencesSessionDataSource {
	override suspend fun hasActiveSession(): Boolean {
		return secureStoreDataSource.contains(PreferencesKeys.USER_ACCESS_TOKEN) ||
					secureStoreDataSource.contains(PreferencesKeys.USER_REFRESH_TOKEN)
	}

	override suspend fun setUsbId(usbId: String) {
		secureStoreDataSource.putString(PreferencesKeys.USER_USB_ID, usbId)
	}

	override suspend fun setAccessToken(accessToken: String) {
		secureStoreDataSource.putString(PreferencesKeys.USER_ACCESS_TOKEN, accessToken)
	}

	override suspend fun setRefreshToken(refreshToken: String) {
		secureStoreDataSource.putString(PreferencesKeys.USER_REFRESH_TOKEN, refreshToken)
	}

	override suspend fun getUsbId(): String? {
		return secureStoreDataSource.getString(PreferencesKeys.USER_USB_ID)
	}

	override suspend fun getAccessToken(): String? {
		return secureStoreDataSource.getString(PreferencesKeys.USER_ACCESS_TOKEN)
	}

	override suspend fun getRefreshToken(): String? {
		return secureStoreDataSource.getString(PreferencesKeys.USER_REFRESH_TOKEN)
	}

	override suspend fun clear() {
		secureStoreDataSource.clear()
	}
}
