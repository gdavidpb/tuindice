package com.gdavidpb.tuindice.record.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.presentation.mapper.NewStudentNoRecordTexts
import com.gdavidpb.tuindice.record.domain.usecase.DeleteSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.EnsureRecordLoadedUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveNewStudentNoRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveSyntheticTermRejectionsUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetRecordViewModeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSelectedTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpsertAttemptSelectionUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.machine.RecordMachine
import com.gdavidpb.tuindice.record.presentation.model.RecordFailedArt
import com.gdavidpb.tuindice.record.testing.ControllableAcademicRecordRepository
import com.gdavidpb.tuindice.record.testing.RecordingRecordSelectionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.awaitUntilState
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

// A failed refresh resolves its message through getString, so like the other UiTests this only
// runs on iOS (androidHostTestExcludedPatterns).
class RecordRefreshFailureUiTest {
	@Test
	fun when_theRefreshFailsForAStudentWithNoRecordYet_then_failedSaysSoFromTheStart() = runTest {
		val fixture = createFixture(syncStatus = SyncStatus.NewStudentNoRecord)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			// First getString of the suite: a cold compose-resources load on the simulator can
			// exceed Turbine's 3s default.
			fixture.viewModel.state.test(timeout = 15.seconds) {
				fixture.viewModel.ensureRecordLoadedAction()

				// The sync had already said it when the refresh failed: the failure brings its reason.
				val failed = awaitUntilState<Record.State.Failed>()

				assertEquals(RecordFailedArt.NoRecord, failed.art)
				assertEquals(NewStudentNoRecordTexts.title, failed.title)
				assertEquals(NewStudentNoRecordTexts.message, failed.message)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun when_theSyncLearnsThereIsNoRecordAfterTheFailure_then_failedChangesWhatItSays() = runTest {
		val fixture = createFixture(syncStatus = SyncStatus.Healthy)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test(timeout = 15.seconds) {
				fixture.viewModel.ensureRecordLoadedAction()

				assertEquals(RecordFailedArt.Error, awaitUntilState<Record.State.Failed>().art)

				fixture.syncStatusRepository.emitSyncStatus(SyncStatus.NewStudentNoRecord)

				val failed = awaitUntilState<Record.State.Failed> { state ->
					state.art == RecordFailedArt.NoRecord
				}

				assertEquals(NewStudentNoRecordTexts.title, failed.title)

				// And back, when a later sync stops saying it.
				fixture.syncStatusRepository.emitSyncStatus(SyncStatus.Failed)

				awaitUntilState<Record.State.Failed> { state -> state.art == RecordFailedArt.Error }

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	// An account with nothing stored whose refresh fails: what a new student's first load looks like.
	private fun createFixture(syncStatus: SyncStatus): RefreshFailureFixture {
		val academicRecordRepository = ControllableAcademicRecordRepository(
			initialRecord = AcademicRecord(id = "record"),
			initialHasSynced = false
		).apply {
			recordAvailable = false
			updateAcademicRecordThrowable = RuntimeException("record unavailable")
		}
		val syncStatusRepository = FakeSyncStatusRepository(initialValue = syncStatus)

		return RefreshFailureFixture(
			viewModel = RecordViewModel(
				screenMachine = createMachine(
					academicRecordRepository = academicRecordRepository,
					syncStatusRepository = syncStatusRepository
				),
				eventPublisher = NoOpEventPublisher
			),
			syncStatusRepository = syncStatusRepository
		)
	}

	private fun createMachine(
		academicRecordRepository: ControllableAcademicRecordRepository,
		syncStatusRepository: FakeSyncStatusRepository
	): RecordMachine {
		val selectionRepository = RecordingRecordSelectionRepository()
		val reportingRepository = RecordingReportingRepository()
		val exceptionHandler = RecordExceptionHandler()

		return RecordMachine(
			observeRecordUseCase = ObserveRecordUseCase(
				academicRecordRepository = academicRecordRepository,
				recordSelectionRepository = selectionRepository,
				syncStatusRepository = syncStatusRepository,
				reportingRepository = reportingRepository
			),
			observeNewStudentNoRecordUseCase = ObserveNewStudentNoRecordUseCase(
				syncStatusRepository = syncStatusRepository,
				reportingRepository = reportingRepository
			),
			observeSyntheticTermRejectionsUseCase = ObserveSyntheticTermRejectionsUseCase(
				academicRecordRepository = academicRecordRepository,
				reportingRepository = reportingRepository
			),
			ensureRecordLoadedUseCase = EnsureRecordLoadedUseCase(
				academicRecordRepository = academicRecordRepository,
				reportingRepository = reportingRepository,
				exceptionHandler = exceptionHandler
			),
			updateRecordUseCase = UpdateRecordUseCase(
				academicRecordRepository = academicRecordRepository,
				reportingRepository = reportingRepository,
				exceptionHandler = exceptionHandler
			),
			setRecordViewModeUseCase = SetRecordViewModeUseCase(
				recordSelectionRepository = selectionRepository,
				reportingRepository = reportingRepository
			),
			setSelectedTermUseCase = SetSelectedTermUseCase(
				recordSelectionRepository = selectionRepository,
				reportingRepository = reportingRepository
			),
			upsertAttemptSelectionUseCase = UpsertAttemptSelectionUseCase(
				academicRecordRepository = academicRecordRepository,
				reportingRepository = reportingRepository,
				exceptionHandler = exceptionHandler
			),
			deleteSyntheticTermUseCase = DeleteSyntheticTermUseCase(
				academicRecordRepository = academicRecordRepository,
				reportingRepository = reportingRepository,
				exceptionHandler = exceptionHandler
			)
		)
	}

	private class RefreshFailureFixture(
		val viewModel: RecordViewModel,
		val syncStatusRepository: FakeSyncStatusRepository
	)
}
