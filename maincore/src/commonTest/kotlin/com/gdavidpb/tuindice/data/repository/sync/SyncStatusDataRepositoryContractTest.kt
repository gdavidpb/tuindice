package com.gdavidpb.tuindice.data.repository.sync

import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.data.source.sync.SyncStatusDataSource
import com.gdavidpb.tuindice.data.contract.sync.SyncStatusLocalDataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest

class SyncStatusDataRepositoryContractTest {
	@Test
	fun delegatesStatusOperationsToLocalDataSource() = runTest {
		val localDataSource = RecordingSyncStatusLocalDataSource(initialValue = SyncStatus.Failed)
		val repository = SyncStatusDataSource(localDataSource = localDataSource)

		assertEquals(SyncStatus.Failed, repository.getSyncStatus())

		repository.setSyncStatus(SyncStatus.OutdatedCredentials)

		assertEquals(SyncStatus.OutdatedCredentials, repository.getSyncStatus())
		assertEquals(listOf(SyncStatus.OutdatedCredentials), localDataSource.setStatuses)
	}
}

private class RecordingSyncStatusLocalDataSource(
	initialValue: SyncStatus
) : SyncStatusLocalDataSource {
	private val syncStatus = MutableStateFlow(initialValue)
	val setStatuses = mutableListOf<SyncStatus>()

	override fun observeSyncStatus(): Flow<SyncStatus> = syncStatus

	override suspend fun getSyncStatus(): SyncStatus = syncStatus.value

	override suspend fun setSyncStatus(status: SyncStatus) {
		syncStatus.value = status
		setStatuses += status
	}
}
