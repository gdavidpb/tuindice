package com.gdavidpb.tuindice.record.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.record.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSubjectGradeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.GetQuartersExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.SetSubjectGradeExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.validator.SetSubjectGradeParamsValidator
import com.gdavidpb.tuindice.record.presentation.action.LoadQuartersActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.SetSubjectGradeActionProcessor
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_QUARTER
import com.gdavidpb.tuindice.record.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.record.testing.RecordingQuarterRepository
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
	fun publicActions_loadContent_andEmitSnackBarOnInvalidGrade() = runTest {
		val viewModel = createViewModel()
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(Record.State.Loading, awaitItem())

				viewModel.loadQuartersAction()
				val content = assertIs<Record.State.Content>(awaitItem())
				assertEquals(listOf(DEFAULT_RECORD_QUARTER), content.quarters)

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
		return RecordViewModel(
			loadQuartersActionProcessor = LoadQuartersActionProcessor(
				getQuartersUseCase = GetQuartersUseCase(
					quarterRepository = RecordingQuarterRepository(
						quarters = flowOf(listOf(DEFAULT_RECORD_QUARTER))
					),
					exceptionHandler = GetQuartersExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true),
						reportingRepository = RecordingReportingRepository()
					)
				)
			),
			setSubjectGradeActionProcessor = SetSubjectGradeActionProcessor(
				setSubjectGradeUseCase = SetSubjectGradeUseCase(
					quarterRepository = RecordingQuarterRepository(),
					paramsValidator = SetSubjectGradeParamsValidator(),
					exceptionHandler = SetSubjectGradeExceptionHandler(
						reportingRepository = RecordingReportingRepository()
					)
				)
			)
		)
	}
}
