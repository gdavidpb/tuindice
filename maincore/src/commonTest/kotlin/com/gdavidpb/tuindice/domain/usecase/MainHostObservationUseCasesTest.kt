package com.gdavidpb.tuindice.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.testkit.base.repository.FakePendingChangesRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

// What the app host follows through its machine instead of reading the repositories from the
// route: the sync status, the session being ended from outside, and the pending work that is read
// before offering to sign out.
class MainHostObservationUseCasesTest {
	@Test
	fun observeSyncStatus_emitsTheCurrentStatus_thenEveryChange() = runTest {
		val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.Healthy)
		val useCase = ObserveSyncStatusUseCase(
			syncStatusRepository = syncStatusRepository,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertIs<UseCaseState.Loading>(awaitItem())
			assertEquals(UseCaseState.Data(SyncStatus.Healthy), awaitItem())

			syncStatusRepository.setSyncStatus(SyncStatus.OutdatedCredentials)
			assertEquals(UseCaseState.Data(SyncStatus.OutdatedCredentials), awaitItem())

			expectNoEvents()
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun observeSessionInvalidation_emitsEachInvalidation_whileObserved() = runTest {
		val sessionInvalidationRepository = FakeSessionInvalidationRepository()
		val useCase = ObserveSessionInvalidationUseCase(
			sessionInvalidationRepository = sessionInvalidationRepository,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertIs<UseCaseState.Loading>(awaitItem())
			expectNoEvents()

			sessionInvalidationRepository.notifySessionInvalidated()
			assertEquals(UseCaseState.Data(Unit), awaitItem())

			expectNoEvents()
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun getPendingChanges_emitsWhatTheDeviceStillHasToSend() = runTest {
		val pendingChanges = PendingChanges(
			totalCount = 3,
			recordCount = 1,
			evaluationsCount = 2,
			hasFailedMutations = true
		)
		val useCase = GetPendingChangesUseCase(
			pendingChangesRepository = FakePendingChangesRepository(pendingChanges = pendingChanges),
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertIs<UseCaseState.Loading>(awaitItem())
			assertEquals(UseCaseState.Data(pendingChanges), awaitItem())
			awaitComplete()
		}
	}

	@Test
	fun getPendingChanges_reportsAndEmitsError_whenTheReadFails() = runTest {
		val failure = IllegalStateException("main-pending-changes")
		val reportingRepository = RecordingReportingRepository()
		val useCase = GetPendingChangesUseCase(
			pendingChangesRepository = FakePendingChangesRepository(getPendingChangesThrowable = failure),
			reportingRepository = reportingRepository
		)

		useCase.execute(Unit).test {
			assertIs<UseCaseState.Loading>(awaitItem())
			assertIs<UseCaseState.Error<*>>(awaitItem())
			awaitComplete()
		}

		assertEquals(listOf<Throwable>(failure), reportingRepository.loggedExceptions)
	}
}
