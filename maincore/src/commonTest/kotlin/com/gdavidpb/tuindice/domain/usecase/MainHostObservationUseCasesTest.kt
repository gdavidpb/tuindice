package com.gdavidpb.tuindice.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.testkit.base.repository.FakePendingChangesRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
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
		val settingsRepository = FakeSettingsRepository()
		val useCase = ObserveSessionInvalidationUseCase(
			sessionInvalidationRepository = sessionInvalidationRepository,
			settingsRepository = settingsRepository,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertIs<UseCaseState.Loading>(awaitItem())
			expectNoEvents()

			// The invalidation leaves its notice persisted before it tells anyone.
			settingsRepository.setSessionResetNoticePending()
			sessionInvalidationRepository.notifySessionInvalidated()
			assertEquals(UseCaseState.Data(Unit), awaitItem())

			expectNoEvents()
			cancelAndIgnoreRemainingEvents()
		}
	}

	// The start-up already told the user (it read the notice the invalidation left): the live
	// signal that arrives afterwards is the same notice, not a second one.
	@Test
	fun observeSessionInvalidation_whenTheStartUpAlreadyShowedTheNotice_doesNotShowItAgain() = runTest {
		val sessionInvalidationRepository = FakeSessionInvalidationRepository()
		val settingsRepository = FakeSettingsRepository()
		val useCase = ObserveSessionInvalidationUseCase(
			sessionInvalidationRepository = sessionInvalidationRepository,
			settingsRepository = settingsRepository,
			reportingRepository = RecordingReportingRepository()
		)
		settingsRepository.setSessionResetNoticePending()
		settingsRepository.consumeSessionResetNoticePending()

		useCase.execute(Unit).test {
			assertIs<UseCaseState.Loading>(awaitItem())

			sessionInvalidationRepository.notifySessionInvalidated()

			expectNoEvents()
			cancelAndIgnoreRemainingEvents()
		}
	}

	// Told is not shown: the notice stays persisted until the interface says it showed it, so a
	// process that dies in between (a background kill) still explains the sign-in screen at the
	// next start.
	@Test
	fun observeSessionInvalidation_keepsTheNoticePending_untilTheInterfaceAcknowledgesIt() = runTest {
		val sessionInvalidationRepository = FakeSessionInvalidationRepository()
		val settingsRepository = FakeSettingsRepository()
		val useCase = ObserveSessionInvalidationUseCase(
			sessionInvalidationRepository = sessionInvalidationRepository,
			settingsRepository = settingsRepository,
			reportingRepository = RecordingReportingRepository()
		)
		settingsRepository.setSessionResetNoticePending()

		useCase.execute(Unit).test {
			assertIs<UseCaseState.Loading>(awaitItem())

			sessionInvalidationRepository.notifySessionInvalidated()
			assertEquals(UseCaseState.Data(Unit), awaitItem())

			assertEquals(true, settingsRepository.sessionResetNoticePending)
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
