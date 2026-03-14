package com.gdavidpb.tuindice.data.repository.sync.source

import com.gdavidpb.tuindice.base.domain.repository.OutdatedCredentialsRepository
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.data.repository.sync.SyncSettingsLocalDataSource
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Duration.Companion.days
import com.gdavidpb.tuindice.evaluations.utils.PreferencesKeys as EvaluationsPreferencesKeys
import com.gdavidpb.tuindice.record.utils.PreferencesKeys as RecordPreferencesKeys
import com.gdavidpb.tuindice.summary.utils.PreferencesKeys as SummaryPreferencesKeys

class SyncSettingsDataSource(
	private val settings: Settings
) : SyncSettingsLocalDataSource, OutdatedCredentialsRepository {
	private val outdatedCredentials = MutableStateFlow(
		settings.getBoolean(
			key = PreferencesKeys.OUTDATED_CREDENTIALS,
			defaultValue = false
		)
	)

	override suspend fun isSyncOnCooldown(): Boolean {
		val cooldownTime = settings.getLongOrNull(PreferencesKeys.COOLDOWN_SYNC) ?: 0L

		return cooldownTime >= currentTimeMillis()
	}

	override suspend fun setSyncOnCooldown() {
		val cooldownTime = currentTimeMillis() + CooldownTimes.COOLDOWN_SYNC

		settings.putLong(PreferencesKeys.COOLDOWN_SYNC, cooldownTime)
	}

	override suspend fun clearFeatureCooldowns() {
		settings.remove(SummaryPreferencesKeys.COOLDOWN_GET_USER)
		settings.remove(RecordPreferencesKeys.COOLDOWN_GET_QUARTERS)
		settings.remove(EvaluationsPreferencesKeys.COOLDOWN_GET_EVALUATIONS)
	}

	override fun observeOutdatedCredentials(): Flow<Boolean> {
		return outdatedCredentials
	}

	override suspend fun hasOutdatedCredentials(): Boolean {
		return outdatedCredentials.value
	}

	override suspend fun setOutdatedCredentials() {
		settings.putBoolean(
			key = PreferencesKeys.OUTDATED_CREDENTIALS,
			value = true
		)
		outdatedCredentials.value = true
	}

	override suspend fun clearOutdatedCredentials() {
		settings.putBoolean(
			key = PreferencesKeys.OUTDATED_CREDENTIALS,
			value = false
		)
		outdatedCredentials.value = false
	}

	private object PreferencesKeys {
		const val COOLDOWN_SYNC = "cooldownSync"
		const val OUTDATED_CREDENTIALS = "outdatedCredentials"
	}

	private object CooldownTimes {
		val COOLDOWN_SYNC = 1.days.inWholeMilliseconds
	}
}
