package com.gdavidpb.tuindice.record.data.repository.quarter.source

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdavidpb.tuindice.record.data.repository.quarter.SettingsDataSource
import com.gdavidpb.tuindice.record.utils.CooldownTimes
import com.gdavidpb.tuindice.record.utils.PreferencesKeys

class PreferencesDataSource(
	private val sharedPreferences: SharedPreferences
) : SettingsDataSource {
	override suspend fun isGetQuartersOnCooldown(): Boolean {
		val cooldownTime = sharedPreferences.getLong(PreferencesKeys.COOLDOWN_GET_QUARTERS, 0L)

		return cooldownTime >= System.currentTimeMillis()
	}

	override suspend fun setGetQuartersOnCooldown() {
		val cooldownTime = System.currentTimeMillis() + CooldownTimes.COOLDOWN_GET_QUARTERS

		sharedPreferences.edit {
			putLong(PreferencesKeys.COOLDOWN_GET_QUARTERS, cooldownTime)
		}
	}
}