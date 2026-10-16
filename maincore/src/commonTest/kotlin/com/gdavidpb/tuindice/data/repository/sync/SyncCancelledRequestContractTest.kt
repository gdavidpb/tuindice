package com.gdavidpb.tuindice.data.repository.sync

import com.gdavidpb.tuindice.base.domain.model.SyncPolicy
import com.gdavidpb.tuindice.data.source.sync.SyncDataSource
import com.gdavidpb.tuindice.data.source.sync.SyncResultLocalDataSource
import com.gdavidpb.tuindice.testing.NoOpRecordOutboxDataRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

// The sync runs in the session scope, which the end of the session cancels before it wipes anything.
@OptIn(ExperimentalCoroutinesApi::class)
class SyncCancelledRequestContractTest {
	// The request comes back with a result while the sync was cancelled (the end of the session cancelled
	// it, and the request returned without ever checking): nothing suspends between the request and the
	// save, so the sync itself must check, or it writes into the stores of the account that left.
	@Test
	fun scheduleSync_aRequestThatReturnsAfterTheCancellation_savesNothing() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val sessionCoroutineScope = testSessionCoroutineScope(dispatcher)
		val settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false)
		val syncStatusRepository = FakeSyncStatusRepository()
		val remoteDataSource = FakeSyncRemoteDataSource()
		val recordLocalDataSource = FakeAcademicRecordLocalDataRepository()
		val userLocalDataSource = FakeUserLocalDataRepository()
		val repository = SyncDataSource(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = remoteDataSource,
			syncResultLocalDataSource = SyncResultLocalDataSource(
				recordLocalDataSource = recordLocalDataSource,
				userLocalDataSource = userLocalDataSource,
				recordOutboxDataSource = NoOpRecordOutboxDataRepository
			),
			pensumRevalidationRepository = FakePensumRevalidationRepository(),
			evaluationsRevalidationRepository = FakeEvaluationsRevalidationRepository(),
			coroutineScope = sessionCoroutineScope
		)
		// The end of the session cancels the sync as the request is made; the request does not notice.
		remoteDataSource.onSyncStarted = {
			sessionCoroutineScope.coroutineContext[Job]?.children?.forEach { child -> child.cancel() }
		}

		repository.scheduleSync(password = "stored-secret", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(listOf("stored-secret"), remoteDataSource.syncPasswords, "the request was made and returned")
		assertEquals(emptyList(), recordLocalDataSource.savedRecords, "but its result was not saved")
		assertEquals(emptyList(), userLocalDataSource.updatedUsers)
		assertEquals(false, settingsDataSource.cooldownMarked)
		assertEquals(emptyList(), syncStatusRepository.setStatuses)
	}
}
