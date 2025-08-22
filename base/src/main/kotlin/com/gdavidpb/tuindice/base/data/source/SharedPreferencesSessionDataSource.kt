package com.gdavidpb.tuindice.base.data.source

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdavidpb.tuindice.base.utils.PreferencesKeys

class SharedPreferencesSessionDataSource(
	private val sharedPreferences: SharedPreferences
) : PreferencesSessionDataSource {
	override suspend fun hasActiveSession(): Boolean {
		return sharedPreferences.contains(PreferencesKeys.USER_ACCESS_TOKEN) ||
				sharedPreferences.contains(PreferencesKeys.USER_REFRESH_TOKEN)
	}

	override suspend fun setUsbId(usbId: String) {
		sharedPreferences.edit {
			putString(PreferencesKeys.USER_USB_ID, usbId)
		}
	}

	override suspend fun setAccessToken(accessToken: String) {
		sharedPreferences.edit {
			putString(PreferencesKeys.USER_ACCESS_TOKEN, accessToken)
		}
	}

	override suspend fun setRefreshToken(refreshToken: String) {
		sharedPreferences.edit {
			putString(PreferencesKeys.USER_REFRESH_TOKEN, refreshToken)
		}
	}

	override suspend fun getUsbId(): String? {
		return sharedPreferences.getString(PreferencesKeys.USER_USB_ID, null)
	}

	override suspend fun getAccessToken(): String? {
		return sharedPreferences.getString(PreferencesKeys.USER_ACCESS_TOKEN, null)
	}

	override suspend fun getRefreshToken(): String? {
		return sharedPreferences.getString(PreferencesKeys.USER_REFRESH_TOKEN, null)
	}

	override suspend fun clear() {
		sharedPreferences.edit {
			clear()
		}
	}
}