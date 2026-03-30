package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.evaluations.data.source.SettingsDataSource
import com.gdavidpb.tuindice.evaluations.utils.CooldownTimes
import com.gdavidpb.tuindice.evaluations.utils.PreferencesKeys
import com.russhwolf.settings.Settings

class LocalSettingsDataSource(
	private val settings: Settings
) : SettingsDataSource {
	override suspend fun isGetEvaluationsOnCooldown(): Boolean {
		val cooldownTime = settings.getLongOrNull(PreferencesKeys.COOLDOWN_GET_EVALUATIONS) ?: 0L

		return cooldownTime >= currentTimeMillis()
	}

	override suspend fun setGetEvaluationsOnCooldown() {
		val cooldownTime = currentTimeMillis() + CooldownTimes.COOLDOWN_GET_EVALUATIONS

		settings.putLong(PreferencesKeys.COOLDOWN_GET_EVALUATIONS, cooldownTime)
	}
}
