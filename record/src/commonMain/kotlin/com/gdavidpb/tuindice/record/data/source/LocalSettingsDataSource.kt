package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.record.data.source.QuarterSettingsDataSource
import com.gdavidpb.tuindice.record.utils.CooldownTimes
import com.gdavidpb.tuindice.record.utils.PreferencesKeys
import com.russhwolf.settings.Settings

class LocalSettingsDataSource(
	private val settings: Settings
) : QuarterSettingsDataSource {
	override suspend fun isGetQuartersOnCooldown(): Boolean {
		val cooldownTime = settings.getLongOrNull(PreferencesKeys.COOLDOWN_GET_QUARTERS) ?: 0L

		return cooldownTime >= currentTimeMillis()
	}

	override suspend fun setGetQuartersOnCooldown() {
		val cooldownTime = currentTimeMillis() + CooldownTimes.COOLDOWN_GET_QUARTERS

		settings.putLong(PreferencesKeys.COOLDOWN_GET_QUARTERS, cooldownTime)
	}
}
