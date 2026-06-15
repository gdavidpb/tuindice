package com.gdavidpb.tuindice.data.source.sync

import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class SyncStatusSettingsDataSource(
	private val settings: Settings
) : SyncStatusRepository {
	private val syncStatus = MutableStateFlow(
		SyncStatus.entries.firstOrNull { status ->
			status.name == settings.getStringOrNull(PreferencesKeys.SYNC_STATUS)
		} ?: SyncStatus.Healthy
	)
	private val lastSuccessfulSyncAt = MutableStateFlow(
		settings.getLongOrNull(PreferencesKeys.LAST_SUCCESSFUL_SYNC_AT)
	)

	override fun observeSyncStatus(): Flow<SyncStatus> {
		return syncStatus
	}

	override fun observeLastSuccessfulSyncAt(): Flow<Long?> {
		return lastSuccessfulSyncAt
	}

	override suspend fun getSyncStatus(): SyncStatus {
		return syncStatus.value
	}

	override suspend fun getLastSuccessfulSyncAt(): Long? {
		return lastSuccessfulSyncAt.value
	}

	override suspend fun setSyncStatus(status: SyncStatus) {
		settings.putString(
			key = PreferencesKeys.SYNC_STATUS,
			value = status.name
		)
		syncStatus.value = status
	}

	override suspend fun setLastSuccessfulSyncAt(timestamp: Long) {
		settings.putLong(
			key = PreferencesKeys.LAST_SUCCESSFUL_SYNC_AT,
			value = timestamp
		)
		lastSuccessfulSyncAt.value = timestamp
	}

	override suspend fun reset() {
		settings.remove(PreferencesKeys.SYNC_STATUS)
		settings.remove(PreferencesKeys.LAST_SUCCESSFUL_SYNC_AT)
		syncStatus.value = SyncStatus.Healthy
		lastSuccessfulSyncAt.value = null
	}

	private object PreferencesKeys {
		const val SYNC_STATUS = "syncStatus"
		const val LAST_SUCCESSFUL_SYNC_AT = "lastSuccessfulSyncAt"
	}
}
