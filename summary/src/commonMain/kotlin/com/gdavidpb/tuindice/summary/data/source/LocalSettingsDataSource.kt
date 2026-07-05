package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.summary.data.repository.user.SettingsDataRepository
import com.gdavidpb.tuindice.summary.utils.CooldownTimes
import com.gdavidpb.tuindice.summary.utils.PreferencesKeys
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class LocalSettingsDataSource(
	private val settings: Settings
) : SettingsDataRepository {
	private val profilePictureVersion = MutableStateFlow(
		settings.getIntOrNull(PreferencesKeys.PROFILE_PICTURE_VERSION) ?: 0
	)

	override suspend fun isGetUserOnCooldown(): Boolean {
		val cooldownTime = settings.getLongOrNull(PreferencesKeys.COOLDOWN_GET_USER) ?: 0L

		return cooldownTime >= currentTimeMillis()
	}

	override suspend fun setGetUserOnCooldown() {
		val cooldownTime = currentTimeMillis() + CooldownTimes.COOLDOWN_GET_USER

		settings.putLong(PreferencesKeys.COOLDOWN_GET_USER, cooldownTime)
	}

	override fun observeProfilePictureVersion(): Flow<Int> {
		return profilePictureVersion
	}

	override suspend fun bumpProfilePictureVersion() {
		val nextVersion = profilePictureVersion.value + 1

		settings.putInt(PreferencesKeys.PROFILE_PICTURE_VERSION, nextVersion)
		profilePictureVersion.value = nextVersion
	}
}
