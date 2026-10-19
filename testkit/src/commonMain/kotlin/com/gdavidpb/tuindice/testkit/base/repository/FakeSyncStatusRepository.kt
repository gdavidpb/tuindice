package com.gdavidpb.tuindice.testkit.base.repository

import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSyncStatusRepository(
	initialValue: SyncStatus = SyncStatus.Healthy,
	initialReport: SyncReport = SyncReport.success(),
	initialLastSuccessfulSyncAt: Long? = null
) : SyncStatusRepository {
	private val syncStatus = MutableStateFlow(initialValue)
	private val syncReport = MutableStateFlow(initialReport)
	private val lastSuccessfulSyncAt = MutableStateFlow(initialLastSuccessfulSyncAt)
	val setStatuses = mutableListOf<SyncStatus>()
	val setReports = mutableListOf<SyncReport>()
	val setLastSuccessfulSyncTimestamps = mutableListOf<Long>()
	var resetCalls = 0
		private set

	override fun observeSyncStatus(): Flow<SyncStatus> = syncStatus

	override fun observeSyncReport(): Flow<SyncReport> = syncReport

	override fun observeLastSuccessfulSyncAt(): Flow<Long?> = lastSuccessfulSyncAt

	override suspend fun getSyncStatus(): SyncStatus = syncStatus.value

	override suspend fun getSyncReport(): SyncReport = syncReport.value

	override suspend fun getLastSuccessfulSyncAt(): Long? = lastSuccessfulSyncAt.value

	override suspend fun setSyncStatus(status: SyncStatus) {
		syncStatus.value = status
		setStatuses += status
	}

	fun emitSyncStatus(status: SyncStatus) {
		syncStatus.value = status
		setStatuses += status
	}

	override suspend fun setSyncReport(report: SyncReport) {
		syncReport.value = report
		setReports += report
	}

	override suspend fun setLastSuccessfulSyncAt(timestamp: Long) {
		lastSuccessfulSyncAt.value = timestamp
		setLastSuccessfulSyncTimestamps += timestamp
	}

	override suspend fun reset() {
		syncStatus.value = SyncStatus.Healthy
		syncReport.value = SyncReport.success()
		lastSuccessfulSyncAt.value = null
		resetCalls++
	}
}
