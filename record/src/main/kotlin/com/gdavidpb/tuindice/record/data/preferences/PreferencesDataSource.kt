package com.gdavidpb.tuindice.record.data.preferences

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdavidpb.tuindice.record.data.quarter.source.SettingsDataSource
import com.gdavidpb.tuindice.record.utils.CooldownTimes
import com.gdavidpb.tuindice.record.utils.PreferencesKeys

class PreferencesDataSource(
	private val sharedPreferences: SharedPreferences
) : SettingsDataSource {
	override suspend fun isGetQuartersOnCooldown(): Boolean {
		val cooldownTime = sharedPreferences.getLong(PreferencesKeys.COOLDOWN_GET_QUARTERS, 0L)

		return cooldownTime <= System.currentTimeMillis()
	}

	override suspend fun setGetQuartersCooldown() {
		val cooldownTime = System.currentTimeMillis() + CooldownTimes.COOLDOWN_GET_QUARTERS

		sharedPreferences.edit {
			putLong(PreferencesKeys.COOLDOWN_GET_QUARTERS, cooldownTime)
		}
	}
}