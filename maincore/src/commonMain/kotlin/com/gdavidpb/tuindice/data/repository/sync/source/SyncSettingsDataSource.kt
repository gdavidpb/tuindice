package com.gdavidpb.tuindice.data.repository.sync.source

import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
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
) : SyncSettingsLocalDataSource, SyncStatusRepository {
	private val syncStatus = MutableStateFlow(
		SyncStatus.entries.firstOrNull { status ->
			status.name == settings.getStringOrNull(PreferencesKeys.SYNC_STATUS)
		} ?: SyncStatus.Healthy
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

	override fun observeSyncStatus(): Flow<SyncStatus> {
		return syncStatus
	}

	override suspend fun getSyncStatus(): SyncStatus {
		return syncStatus.value
	}

	override suspend fun setSyncStatus(status: SyncStatus) {
		settings.putString(
			key = PreferencesKeys.SYNC_STATUS,
			value = status.name
		)
		syncStatus.value = status
	}

	private object PreferencesKeys {
		const val COOLDOWN_SYNC = "cooldownSync"
		const val SYNC_STATUS = "syncStatus"
	}

	private object CooldownTimes {
		val COOLDOWN_SYNC = 1.days.inWholeMilliseconds
	}
}
