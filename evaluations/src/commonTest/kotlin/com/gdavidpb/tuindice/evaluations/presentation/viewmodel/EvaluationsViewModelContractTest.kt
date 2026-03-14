package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationSubjectFilter
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.GetEvaluationsExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.RemoveEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.*
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.evaluations.testing.SECOND_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class EvaluationsViewModelContractTest {
	@Test
	fun publicActions_loadContent_updateFilters_andEmitNavigationEffect() = runTest {
		val viewModel = createViewModel(testScheduler)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)
		val filter = EvaluationSubjectFilter(DEFAULT_EVALUATION_SUBJECT.code)

		try {
			viewModel.state.test {
				assertEquals(Evaluations.State.Loading, awaitItem())

				viewModel.loadEvaluationsAction()
				val content = assertIs<Evaluations.State.Content>(awaitItem())
				assertEquals(2, content.filteredEvaluations.size)

				viewModel.toggleFilterAction(filter, isChecked = true)
				val filtered = assertIs<Evaluations.State.Content>(awaitItem())
				assertEquals(listOf(filter), filtered.activeFilters)

				cancelAndIgnoreRemainingEvents()
			}

			viewModel.effect.test {
				viewModel.addEvaluationAction()
				assertIs<Evaluations.Effect.NavigateToAddEvaluation>(awaitItem())

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	private fun createViewModel(testScheduler: TestCoroutineScheduler): EvaluationsViewModel {
		val repository = RecordingEvaluationRepository(
			evaluationsFlow = kotlinx.coroutines.flow.flowOf(
				listOf(
					com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION,
					com.gdavidpb.tuindice.evaluations.testing.DEFAULT_COMPLETED_EVALUATION
				)
			),
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT)
		)

		return EvaluationsViewModel(
			loadEvaluationsActionProcessor = LoadEvaluationsActionProcessor(
				getEvaluationsUseCase = GetEvaluationsUseCase(
					evaluationRepository = repository,
					exceptionHandler = GetEvaluationsExceptionHandler(
						reportingRepository = RecordingReportingRepository()
					)
				)
			),
			checkEvaluationFilterActionProcessor = CheckEvaluationFilterActionProcessor(),
			uncheckEvaluationFilterActionProcessor = UncheckEvaluationFilterActionProcessor(),
			clearEvaluationFiltersActionProcessor = ClearEvaluationFiltersActionProcessor(),
			openAddEvaluationActionProcessor = OpenAddEvaluationActionProcessor(),
			pickEvaluationGradeActionProcessor = PickEvaluationGradeActionProcessor(
				getEvaluationUseCase = GetEvaluationUseCase(repository)
			),
			setEvaluationGradeActionProcessor = SetEvaluationGradeActionProcessor(
				updateEvaluationUseCase = UpdateEvaluationUseCase(
					evaluationRepository = repository,
					exceptionHandler = UpdateEvaluationExceptionHandler(
						reportingRepository = RecordingReportingRepository()
					)
				)
			),
			openEvaluationActionProcessor = OpenEvaluationActionProcessor(),
			removeEvaluationActionProcessor = RemoveEvaluationActionProcessor(
				removeEvaluationUseCase = RemoveEvaluationUseCase(
					evaluationRepository = repository,
					exceptionHandler = RemoveEvaluationExceptionHandler(
						reportingRepository = RecordingReportingRepository()
					)
				)
			)
		)
	}
}
