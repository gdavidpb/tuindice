package com.gdavidpb.tuindice.data.source.sync

import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json

class SyncStatusSettingsDataSource(
	private val settings: Settings
) : SyncStatusRepository, SessionMemory {
	private val syncStatus = MutableStateFlow(loadSyncStatus())
	private val lastSuccessfulSyncAt = MutableStateFlow(
		settings.getLongOrNull(PreferencesKeys.LAST_SUCCESSFUL_SYNC_AT)
	)
	private val syncReport = MutableStateFlow(loadSyncReport())

	override fun observeSyncStatus(): Flow<SyncStatus> {
		return syncStatus
	}

	override fun observeSyncReport(): Flow<SyncReport> {
		return syncReport
	}

	override fun observeLastSuccessfulSyncAt(): Flow<Long?> {
		return lastSuccessfulSyncAt
	}

	override suspend fun getSyncStatus(): SyncStatus {
		return syncStatus.value
	}

	override suspend fun getSyncReport(): SyncReport {
		return syncReport.value
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

	override suspend fun setSyncReport(report: SyncReport) {
		settings.putString(
			key = PreferencesKeys.SYNC_REPORT,
			value = syncReportSettingsJson.encodeToString(report)
		)
		syncReport.value = report
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
		settings.remove(PreferencesKeys.SYNC_REPORT)
		settings.remove(PreferencesKeys.LAST_SUCCESSFUL_SYNC_AT)
		syncStatus.value = SyncStatus.Healthy
		syncReport.value = SyncReport.success()
		lastSuccessfulSyncAt.value = null
	}

	// The mirrors go back to what is stored, which the wipe has just emptied.
	override suspend fun clearSessionMemory() {
		syncStatus.value = loadSyncStatus()
		syncReport.value = loadSyncReport()
		lastSuccessfulSyncAt.value = settings.getLongOrNull(PreferencesKeys.LAST_SUCCESSFUL_SYNC_AT)
	}

	private fun loadSyncStatus(): SyncStatus {
		return SyncStatus.entries.firstOrNull { status ->
			status.name == settings.getStringOrNull(PreferencesKeys.SYNC_STATUS)
		} ?: SyncStatus.Healthy
	}

	private fun loadSyncReport(): SyncReport {
		return settings.getStringOrNull(PreferencesKeys.SYNC_REPORT)
			?.let { rawReport ->
				runCatching {
					syncReportSettingsJson.decodeFromString<SyncReport>(rawReport)
				}.getOrNull()
			}
			?: SyncReport.success()
	}

	private object PreferencesKeys {
		const val SYNC_STATUS = "syncStatus"
		const val SYNC_REPORT = "syncReport"
		const val LAST_SUCCESSFUL_SYNC_AT = "lastSuccessfulSyncAt"
	}
}

private val syncReportSettingsJson = Json {
	ignoreUnknownKeys = true
}
