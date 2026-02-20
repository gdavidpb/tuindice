package com.gdavidpb.tuindice.summary.data.repository.user.source

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdavidpb.tuindice.summary.data.repository.user.SettingsDataSource
import com.gdavidpb.tuindice.summary.utils.CooldownTimes
import com.gdavidpb.tuindice.summary.utils.PreferencesKeys

class PreferencesDataSource(
	private val sharedPreferences: SharedPreferences
) : SettingsDataSource {
	override suspend fun isGetUserOnCooldown(): Boolean {
		val cooldownTime = sharedPreferences.getLong(PreferencesKeys.COOLDOWN_GET_USER, 0L)

		return cooldownTime >= System.currentTimeMillis()
	}

	override suspend fun setGetUserOnCooldown() {
		val cooldownTime = System.currentTimeMillis() + CooldownTimes.COOLDOWN_GET_USER

		sharedPreferences.edit {
			putLong(PreferencesKeys.COOLDOWN_GET_USER, cooldownTime)
		}
	}
}