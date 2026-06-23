package com.gdavidpb.tuindice.record.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.record.domain.usecase.DeleteSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.EnsureRecordLoadedUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetRecordViewModeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSelectedTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpsertAttemptSelectionUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.machine.RecordMachine
import com.gdavidpb.tuindice.record.testing.ControllableAcademicRecordRepository
import com.gdavidpb.tuindice.record.testing.RecordingRecordSelectionRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest

// Resolves snack messages through getString, so it runs on the iOS host only
// (androidHostTestExcludedPatterns) — same constraint as the SubjectDetail oracle.
class RecordViewModelSnackContractTest {
	@Test
	fun deleteSyntheticTerm_emitsSnackBar_afterDeleting() = runTest {
		val academicRecordRepository = ControllableAcademicRecordRepository(
			initialRecord = AcademicRecord(id = "record"),
			initialHasSynced = true
		)
		val selectionRepository = RecordingRecordSelectionRepository()
		val reportingRepository = RecordingReportingRepository()
		val exceptionHandler = RecordExceptionHandler()

		val viewModel = RecordViewModel(
			screenMachine = RecordMachine(
				observeRecordUseCase = ObserveRecordUseCase(
					academicRecordRepository = academicRecordRepository,
					recordSelectionRepository = selectionRepository,
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
			),
			eventPublisher = NoOpEventPublisher
		)

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			// First getString of the suite: cold compose-resources load on the simulator
			// can exceed Turbine's 3s default now that no earlier test warms it up.
			viewModel.effect.test(timeout = 15.seconds) {
				viewModel.deleteSyntheticTermAction(termId = "synthetic-term")

				val snack = assertIs<Record.Effect.ShowSnackBar>(awaitItem())
				assertTrue(snack.message.isNotBlank())

				cancelAndIgnoreRemainingEvents()
			}

			assertEquals(
				listOf("synthetic-term"),
				academicRecordRepository.deletedTermIds
			)
		} finally {
			stateCollector.cancel()
		}
	}
}
