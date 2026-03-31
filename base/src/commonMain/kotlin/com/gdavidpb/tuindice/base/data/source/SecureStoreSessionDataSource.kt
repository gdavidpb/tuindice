package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.data.contract.PreferencesSessionDataSource

import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import eu.anifantakis.lib.ksafe.KSafe

class SecureStoreSessionDataSource(
	private val kSafe: KSafe
) : PreferencesSessionDataSource {
	override suspend fun hasActiveSession(): Boolean {
		return kSafe.getDirect<String?>(key = PreferencesKeys.USER_ACCESS_TOKEN, defaultValue = null) != null ||
					kSafe.getDirect<String?>(key = PreferencesKeys.USER_REFRESH_TOKEN, defaultValue = null) != null
	}

	override suspend fun setUsbId(usbId: String) {
		kSafe.putDirect(key = PreferencesKeys.USER_USB_ID, value = usbId)
	}

	override suspend fun setAccessToken(accessToken: String) {
		kSafe.putDirect(key = PreferencesKeys.USER_ACCESS_TOKEN, value = accessToken)
	}

	override suspend fun setRefreshToken(refreshToken: String) {
		kSafe.putDirect(key = PreferencesKeys.USER_REFRESH_TOKEN, value = refreshToken)
	}

	override suspend fun getUsbId(): String? {
		return kSafe.getDirect<String?>(key = PreferencesKeys.USER_USB_ID, defaultValue = null)
	}

	override suspend fun getAccessToken(): String? {
		return kSafe.getDirect<String?>(key = PreferencesKeys.USER_ACCESS_TOKEN, defaultValue = null)
	}

	override suspend fun getRefreshToken(): String? {
		return kSafe.getDirect<String?>(key = PreferencesKeys.USER_REFRESH_TOKEN, defaultValue = null)
	}

	override suspend fun clear() {
		kSafe.clearAll()
	}
}
