package com.gdavidpb.tuindice.data.repository.messaging.source

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import com.gdavidpb.tuindice.data.repository.messaging.LocalDataSource

class MessagingPreferencesDataSource(
	private val sharedPreferences: SharedPreferences
) : LocalDataSource {
	override suspend fun isSubscribed(): Boolean {
		return sharedPreferences
			.getBoolean(PreferencesKeys.IS_SUBSCRIBED, false)
	}

	override suspend fun markAsSubscribed() {
		sharedPreferences.edit {
			putBoolean(PreferencesKeys.IS_SUBSCRIBED, true)
		}
	}
}