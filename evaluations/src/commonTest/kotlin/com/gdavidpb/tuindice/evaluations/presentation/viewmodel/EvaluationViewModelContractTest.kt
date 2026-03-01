package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationAndAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.validator.AddEvaluationParamsValidator
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.AddEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.EditEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.LoadAvailableSubjectsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.LoadEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.PickGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.PickMaxGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetDateActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetMaxGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetSubjectActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.SetTypeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.FakeEvaluationTextProvider
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
	fun publicActions_loadSubjects_updateSelection_andEmitGradePickerEffect() = runTest {
		val viewModel = createViewModel()
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(Evaluation.State.Loading, awaitItem())

				viewModel.loadAvailableSubjectsAction()
				val content = assertIs<Evaluation.State.Content>(awaitItem())
				assertEquals(
					listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT),
					content.availableSubjects
				)

				viewModel.setSubjectAction(SECOND_EVALUATION_SUBJECT)
				val selected = assertIs<Evaluation.State.Content>(awaitItem())
				assertEquals(SECOND_EVALUATION_SUBJECT, selected.selectedSubject)

				cancelAndIgnoreRemainingEvents()
			}

			viewModel.effect.test {
				viewModel.clickGradeAction(grade = 15.0, maxGrade = 100.0)
				val effect = assertIs<Evaluation.Effect.NavigateToGradePickerDialog>(awaitItem())
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
		val textProvider = FakeEvaluationTextProvider()

		return EvaluationViewModel(
			loadAvailableSubjectsActionProcessor = LoadAvailableSubjectsActionProcessor(
				getAvailableSubjectsUseCase = GetAvailableSubjectsUseCase(repository)
			),
			loadEvaluationActionProcessor = LoadEvaluationActionProcessor(
				getEvaluationAndAvailableSubjectsUseCase = GetEvaluationAndAvailableSubjectsUseCase(
					repository
				)
			),
			addEvaluationActionProcessor = AddEvaluationActionProcessor(
				addEvaluationUseCase = AddEvaluationUseCase(
					evaluationRepository = repository,
					identifierRepository = FakeIdentifierRepository(),
					paramsValidator = AddEvaluationParamsValidator(),
					exceptionHandler = AddEvaluationExceptionHandler(
						reportingRepository = RecordingReportingRepository()
					)
				),
				textProvider = textProvider
			),
			editEvaluationActionProcessor = EditEvaluationActionProcessor(
				updateEvaluationUseCase = UpdateEvaluationUseCase(repository),
				textProvider = textProvider
			),
			pickGradeActionProcessor = PickGradeActionProcessor(),
			pickMaxGradeActionProcessor = PickMaxGradeActionProcessor(),
			setSubjectActionProcessor = SetSubjectActionProcessor(),
			setTypeActionProcessor = SetTypeActionProcessor(),
			setDateActionProcessor = SetDateActionProcessor(),
			setGradeActionProcessor = SetGradeActionProcessor(),
			setMaxGradeActionProcessor = SetMaxGradeActionProcessor()
		)
	}
}
