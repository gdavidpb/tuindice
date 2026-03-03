package com.gdavidpb.tuindice.summary.data.repository.user.source

import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.summary.data.repository.user.SettingsDataSource
import com.gdavidpb.tuindice.summary.utils.CooldownTimes
import com.gdavidpb.tuindice.summary.utils.PreferencesKeys
import com.russhwolf.settings.Settings

class LocalSettingsDataSource(
	private val settings: Settings
) : SettingsDataSource {
	override suspend fun isGetUserOnCooldown(): Boolean {
		val cooldownTime = settings.getLongOrNull(PreferencesKeys.COOLDOWN_GET_USER) ?: 0L

		return cooldownTime >= currentTimeMillis()
	}

	override suspend fun setGetUserOnCooldown() {
		val cooldownTime = currentTimeMillis() + CooldownTimes.COOLDOWN_GET_USER

		settings.putLong(PreferencesKeys.COOLDOWN_GET_USER, cooldownTime)
	}
}
