package com.gdavidpb.tuindice.record.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.record.domain.usecase.GetRecordViewModeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.GetSelectedQuarterIdUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetRecordViewModeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSelectedQuarterIdUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSubjectGradeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.SetSubjectGradeExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.UpdateQuartersExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.validator.SetSubjectGradeParamsValidator
import com.gdavidpb.tuindice.record.presentation.action.ObserveQuartersActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.RefreshQuartersActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.SetRecordViewModeActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.SelectQuarterActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.SetSubjectGradeActionProcessor
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_QUARTER
import com.gdavidpb.tuindice.record.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.record.testing.RecordingQuarterRepository
import com.gdavidpb.tuindice.record.testing.RecordingQuarterSelectionRepository
import com.gdavidpb.tuindice.record.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.snack_default_error
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RecordViewModelContractTest {
	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun initialAction_observesContent_andInvalidGradeEmitsSnackBar() = runTest {
		val viewModel = createViewModel()
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(Record.State.Loading, awaitItem())

				val content = assertIs<Record.State.Content>(awaitItem())
				assertEquals(listOf(DEFAULT_RECORD_QUARTER), content.quarters)
				assertEquals(DEFAULT_RECORD_QUARTER.id, content.selectedQuarterId)

				cancelAndIgnoreRemainingEvents()
			}

			viewModel.effect.test {
				viewModel.updateSubjectAction(
					quarterId = DEFAULT_RECORD_QUARTER.id,
					subjectId = DEFAULT_RECORD_QUARTER.subjects.single().id,
					grade = -1,
					commit = false
				)

				val effect = assertIs<Record.Effect.ShowSnackBar>(awaitItem())
				assertEquals(getString(Res.string.snack_default_error), effect.message)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	private fun createViewModel(): RecordViewModel {
		val quarterSelectionRepository = RecordingQuarterSelectionRepository()

		return RecordViewModel(
			observeQuartersActionProcessor = ObserveQuartersActionProcessor(
				observeQuartersUseCase = ObserveQuartersUseCase(
					quarterRepository = RecordingQuarterRepository(
						quarters = flowOf(listOf(DEFAULT_RECORD_QUARTER))
					),
					reportingRepository = RecordingReportingRepository()
				),
				getRecordViewModeUseCase = GetRecordViewModeUseCase(
					quarterSelectionRepository = quarterSelectionRepository,
					reportingRepository = RecordingReportingRepository()
				),
				getSelectedQuarterIdUseCase = GetSelectedQuarterIdUseCase(
					quarterSelectionRepository = quarterSelectionRepository,
					reportingRepository = RecordingReportingRepository()
				),
				setSelectedQuarterIdUseCase = SetSelectedQuarterIdUseCase(
					quarterSelectionRepository = quarterSelectionRepository,
					reportingRepository = RecordingReportingRepository()
				)
			),
			refreshQuartersActionProcessor = RefreshQuartersActionProcessor(
				updateQuartersUseCase = UpdateQuartersUseCase(
					quarterRepository = RecordingQuarterRepository(),
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = UpdateQuartersExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true)
					)
				)
			),
			setRecordViewModeActionProcessor = SetRecordViewModeActionProcessor(
				setRecordViewModeUseCase = SetRecordViewModeUseCase(
					quarterSelectionRepository = quarterSelectionRepository,
					reportingRepository = RecordingReportingRepository()
				),
				getSelectedQuarterIdUseCase = GetSelectedQuarterIdUseCase(
					quarterSelectionRepository = quarterSelectionRepository,
					reportingRepository = RecordingReportingRepository()
				),
				setSelectedQuarterIdUseCase = SetSelectedQuarterIdUseCase(
					quarterSelectionRepository = quarterSelectionRepository,
					reportingRepository = RecordingReportingRepository()
				)
			),
			selectQuarterActionProcessor = SelectQuarterActionProcessor(
				setSelectedQuarterIdUseCase = SetSelectedQuarterIdUseCase(
					quarterSelectionRepository = quarterSelectionRepository,
					reportingRepository = RecordingReportingRepository()
				)
			),
			setSubjectGradeActionProcessor = SetSubjectGradeActionProcessor(
				setSubjectGradeUseCase = SetSubjectGradeUseCase(
					quarterRepository = RecordingQuarterRepository(),
					reportingRepository = RecordingReportingRepository(),
					paramsValidator = SetSubjectGradeParamsValidator(),
					exceptionHandler = SetSubjectGradeExceptionHandler()
				)
			)
		)
	}
}
