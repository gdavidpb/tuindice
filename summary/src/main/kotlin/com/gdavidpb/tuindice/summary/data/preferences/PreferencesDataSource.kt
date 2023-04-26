package com.gdavidpb.tuindice.summary.data.preferences

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdavidpb.tuindice.summary.data.account.source.SettingsDataSource
import com.gdavidpb.tuindice.summary.utils.CooldownTimes
import com.gdavidpb.tuindice.summary.utils.PreferencesKeys

class PreferencesDataSource(
	private val sharedPreferences: SharedPreferences
) : SettingsDataSource {
	override suspend fun isGetAccountOnCooldown(): Boolean {
		val cooldownTime = sharedPreferences.getLong(PreferencesKeys.COOLDOWN_GET_ACCOUNT, 0L)

		return cooldownTime >= System.currentTimeMillis()
	}

	override suspend fun setGetAccountOnCooldown() {
		val cooldownTime = System.currentTimeMillis() + CooldownTimes.COOLDOWN_GET_ACCOUNT

		sharedPreferences.edit {
			putLong(PreferencesKeys.COOLDOWN_GET_ACCOUNT, cooldownTime)
		}
	}
}