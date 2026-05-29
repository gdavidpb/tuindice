package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetAvailableAttemptsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationAndAvailableAttemptsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.validator.AddEvaluationParamsValidator
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.AddEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.EditEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.LoadAvailableAttemptsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.LoadEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.PickGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.PickMaxGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetDateActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetMaxGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetAttemptActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetTypeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.FakeIdentifierRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.evaluations.testing.SECOND_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EvaluationViewModelContractTest {
	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun publicActions_loadAttempts_updateSelection_andEmitGradePickerEffect() = runTest {
		val viewModel = createViewModel()
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(Evaluation.State.Loading, awaitItem())

				viewModel.loadAvailableAttemptsAction()
				val content = assertIs<Evaluation.State.Content>(awaitItem())
				assertEquals(
					listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT),
					content.attemptItems.map { item -> item.attempt }
				)

				viewModel.setAttemptAction(SECOND_EVALUATION_SUBJECT)
				val selected = assertIs<Evaluation.State.Content>(awaitItem())
				assertEquals(SECOND_EVALUATION_SUBJECT, selected.selectedAttempt)
				assertEquals(1, selected.attemptItems.count { item -> item.isSelected })

				viewModel.setAttemptAction(null)
				val cleared = assertIs<Evaluation.State.Content>(awaitItem())
				assertEquals(null, cleared.selectedAttempt)
				assertEquals(0, cleared.attemptItems.count { item -> item.isSelected })

				cancelAndIgnoreRemainingEvents()
				}

				viewModel.effect.test {
					viewModel.clickGradeAction(
						evaluationName = "Parcial 1",
						subjectCode = SECOND_EVALUATION_SUBJECT.code,
						grade = 15.0,
						maxGrade = 100.0
					)
					val effect = assertIs<Evaluation.Effect.NavigateToGradePickerDialog>(awaitItem())
					assertEquals("Parcial 1", effect.evaluationName)
					assertEquals(SECOND_EVALUATION_SUBJECT.code, effect.subjectCode)
					assertEquals(15.0, effect.grade)
				assertEquals(100.0, effect.maxGrade)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	private fun createViewModel(): EvaluationViewModel {
		val repository = RecordingEvaluationRepository(
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT)
		)

		return EvaluationViewModel(
			loadAvailableAttemptsActionProcessor = LoadAvailableAttemptsActionProcessor(
				getAvailableAttemptsUseCase = GetAvailableAttemptsUseCase(
					evaluationRepository = repository,
					reportingRepository = RecordingReportingRepository()
				)
			),
			loadEvaluationActionProcessor = LoadEvaluationActionProcessor(
				getEvaluationAndAvailableAttemptsUseCase = GetEvaluationAndAvailableAttemptsUseCase(
					evaluationRepository = repository,
					reportingRepository = RecordingReportingRepository()
				)
			),
			addEvaluationActionProcessor = AddEvaluationActionProcessor(
				addEvaluationUseCase = AddEvaluationUseCase(
					evaluationRepository = repository,
					identifierRepository = FakeIdentifierRepository(),
					reportingRepository = RecordingReportingRepository(),
					paramsValidator = AddEvaluationParamsValidator(),
					exceptionHandler = AddEvaluationExceptionHandler()
				)
			),
			editEvaluationActionProcessor = EditEvaluationActionProcessor(
				updateEvaluationUseCase = UpdateEvaluationUseCase(
					evaluationRepository = repository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = UpdateEvaluationExceptionHandler()
				)
			),
			pickGradeActionProcessor = PickGradeActionProcessor(),
			pickMaxGradeActionProcessor = PickMaxGradeActionProcessor(),
			setAttemptActionProcessor = SetAttemptActionProcessor(),
			setTypeActionProcessor = SetTypeActionProcessor(),
			setDateActionProcessor = SetDateActionProcessor(),
			setGradeActionProcessor = SetGradeActionProcessor(),
			setMaxGradeActionProcessor = SetMaxGradeActionProcessor(),
			eventPublisher = NoOpEventPublisher
		)
	}
}
