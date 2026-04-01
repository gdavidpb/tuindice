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
		const val SYNC_STATUS = "syncStatus"
	}
}
