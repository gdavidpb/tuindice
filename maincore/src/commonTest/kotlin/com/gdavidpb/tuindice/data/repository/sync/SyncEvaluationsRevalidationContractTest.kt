package com.gdavidpb.tuindice.data.repository.sync

import com.gdavidpb.tuindice.base.domain.model.SyncPolicy
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.data.source.sync.SyncDataSource
import com.gdavidpb.tuindice.data.source.sync.SyncResultLocalDataSource
import com.gdavidpb.tuindice.testing.NoOpRecordOutboxDataRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import com.gdavidpb.tuindice.testkit.ktor.serverResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

// The evaluations revalidation a successful sync hands off (SyncDataSource.launchEvaluationsRevalidation).
@OptIn(ExperimentalCoroutinesApi::class)
class SyncEvaluationsRevalidationContractTest {
	@Test
	fun scheduleSync_success_revalidatesTheEvaluations() = runTest {
		val evaluationsRevalidation = FakeEvaluationsRevalidationRepository()
		val repository = createRepository(
			remoteDataSource = FakeSyncRemoteDataSource(),
			evaluationsRevalidationRepository = evaluationsRevalidation
		)

		repository.scheduleSync(password = "secret123", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(1, evaluationsRevalidation.calls)
	}

	@Test
	fun scheduleSync_failure_neverRevalidatesTheEvaluations() = runTest {
		val evaluationsRevalidation = FakeEvaluationsRevalidationRepository()
		val repository = createRepository(
			remoteDataSource = FakeSyncRemoteDataSource(
				throwable = serverResponseException(
					statusCode = HttpStatusCode.BadRequest,
					path = "/record/v5/sync"
				)
			),
			evaluationsRevalidationRepository = evaluationsRevalidation
		)

		repository.scheduleSync(password = "secret123", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(0, evaluationsRevalidation.calls)
	}

	@Test
	fun scheduleSync_aFailingEvaluationsRevalidation_leavesTheSyncHealthy() = runTest {
		val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.Failed)
		val repository = createRepository(
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = FakeSyncRemoteDataSource(),
			evaluationsRevalidationRepository = FakeEvaluationsRevalidationRepository(
				throwable = IllegalStateException("evaluations unavailable")
			)
		)

		repository.scheduleSync(password = "secret123", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(SyncStatus.Healthy, syncStatusRepository.getSyncStatus())
		assertEquals(listOf(SyncStatus.Healthy), syncStatusRepository.setStatuses)
	}

	@Test
	fun scheduleSync_aPendingEvaluationsRevalidation_doesNotHoldTheNextForcedSync() = runTest {
		val remoteDataSource = FakeSyncRemoteDataSource()
		val repository = createRepository(
			remoteDataSource = remoteDataSource,
			evaluationsRevalidationRepository = FakeEvaluationsRevalidationRepository(gate = CompletableDeferred())
		)

		repository.scheduleSync(password = "secret123", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()
		repository.scheduleSync(password = "secret123", policy = SyncPolicy.ForceRefresh)
		advanceUntilIdle()

		assertEquals(listOf("secret123", "secret123"), remoteDataSource.syncPasswords)
		assertEquals(false, repository.observeSyncInProgress().first())
	}

	@Test
	fun cancelActiveWork_cancelsAPendingEvaluationsRevalidation() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val sessionCoroutineScope = testSessionCoroutineScope(dispatcher)
		val evaluationsRevalidation = FakeEvaluationsRevalidationRepository(gate = CompletableDeferred())
		val repository = createRepository(
			remoteDataSource = FakeSyncRemoteDataSource(),
			evaluationsRevalidationRepository = evaluationsRevalidation,
			coroutineScope = sessionCoroutineScope
		)

		repository.scheduleSync(password = "secret123", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()
		// Sign-out cancels session work before it clears local data, revalidation included.
		sessionCoroutineScope.cancelActiveWork()
		advanceUntilIdle()

		assertEquals(1, evaluationsRevalidation.calls)
		assertEquals(true, evaluationsRevalidation.cancelled)
	}

	// The revalidation is launched after the cooldowns are cleared, never before: the retention the
	// revalidation honours is the one that is left once the sync has cleared the stale ones.
	@Test
	fun scheduleSync_launchesTheEvaluationsRevalidation_afterClearingTheStaleCooldowns() = runTest {
		val events = mutableListOf<String>()
		val repository = createRepositoryWith(
			settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false).apply {
				onClearStaleFeatureCooldowns = { events += "clear_stale" }
			},
			evaluationsRevalidationRepository = FakeEvaluationsRevalidationRepository(
				onRevalidate = { events += "revalidate_evaluations" }
			)
		)

		repository.scheduleSync(password = "secret123", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(listOf("clear_stale", "revalidate_evaluations"), events)
	}

	// Between the last suspension of the sync and the launch there are only plain writes: a
	// sign-out that cancels the sync there must not be followed by revalidations nobody cancels.
	@Test
	fun scheduleSync_cancelledInItsFinalStretch_launchesNoRevalidation() = runTest {
		val evaluationsRevalidation = FakeEvaluationsRevalidationRepository()
		val pensumRevalidation = FakePensumRevalidationRepository()
		val repository = createRepositoryWith(
			settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false).apply {
				onClearStaleFeatureCooldowns = { currentCoroutineContext()[Job]?.cancel() }
			},
			evaluationsRevalidationRepository = evaluationsRevalidation,
			pensumRevalidationRepository = pensumRevalidation
		)

		repository.scheduleSync(password = "secret123", policy = SyncPolicy.RespectCooldown)
		advanceUntilIdle()

		assertEquals(0, pensumRevalidation.calls)
		assertEquals(0, evaluationsRevalidation.calls)
		assertEquals(false, repository.observeSyncInProgress().first())
	}

	private fun TestScope.createRepositoryWith(
		settingsDataSource: SyncSettingsLocalDataRepository,
		evaluationsRevalidationRepository: FakeEvaluationsRevalidationRepository,
		pensumRevalidationRepository: FakePensumRevalidationRepository = FakePensumRevalidationRepository()
	): SyncDataSource {
		return SyncDataSource(
			settingsDataSource = settingsDataSource,
			syncStatusRepository = FakeSyncStatusRepository(),
			remoteDataSource = FakeSyncRemoteDataSource(),
			syncResultLocalDataSource = SyncResultLocalDataSource(
				recordLocalDataSource = FakeAcademicRecordLocalDataRepository(),
				userLocalDataSource = FakeUserLocalDataRepository(),
				recordOutboxDataSource = NoOpRecordOutboxDataRepository
			),
			pensumRevalidationRepository = pensumRevalidationRepository,
			evaluationsRevalidationRepository = evaluationsRevalidationRepository,
			coroutineScope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
		)
	}

	private fun TestScope.createRepository(
		syncStatusRepository: SyncStatusRepository = FakeSyncStatusRepository(),
		remoteDataSource: SyncRemoteDataRepository = FakeSyncRemoteDataSource(),
		evaluationsRevalidationRepository: FakeEvaluationsRevalidationRepository,
		coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
	): SyncDataSource {
		return SyncDataSource(
			settingsDataSource = FakeSyncSettingsLocalDataSource(onCooldown = false),
			syncStatusRepository = syncStatusRepository,
			remoteDataSource = remoteDataSource,
			syncResultLocalDataSource = SyncResultLocalDataSource(
				recordLocalDataSource = FakeAcademicRecordLocalDataRepository(),
				userLocalDataSource = FakeUserLocalDataRepository(),
				recordOutboxDataSource = NoOpRecordOutboxDataRepository
			),
			pensumRevalidationRepository = FakePensumRevalidationRepository(),
			evaluationsRevalidationRepository = evaluationsRevalidationRepository,
			coroutineScope = coroutineScope
		)
	}
}
