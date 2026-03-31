package com.gdavidpb.tuindice.data.source.sync

import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.data.contract.sync.SyncSettingsLocalDataSource
import com.russhwolf.settings.Settings
import kotlin.time.Duration.Companion.days
import com.gdavidpb.tuindice.evaluations.utils.PreferencesKeys as EvaluationsPreferencesKeys
import com.gdavidpb.tuindice.record.utils.PreferencesKeys as RecordPreferencesKeys
import com.gdavidpb.tuindice.summary.utils.PreferencesKeys as SummaryPreferencesKeys

class SyncSettingsDataSource(
	private val settings: Settings
) : SyncSettingsLocalDataSource {
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

	private object PreferencesKeys {
		const val COOLDOWN_SYNC = "cooldownSync"
	}

	private object CooldownTimes {
		val COOLDOWN_SYNC = 1.days.inWholeMilliseconds
	}
}
