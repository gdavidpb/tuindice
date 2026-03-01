package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationSubjectFilter
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.GetEvaluationsExceptionHandler
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.CheckEvaluationFilterActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.ClearEvaluationFiltersActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.LoadEvaluationsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.OpenAddEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.OpenEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.PickEvaluationGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.RemoveEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.SetEvaluationGradeActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.UncheckEvaluationFilterActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.FakeEvaluationFilterLabelsProvider
import com.gdavidpb.tuindice.evaluations.testing.FakeEvaluationTextProvider
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.evaluations.testing.SECOND_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EvaluationsViewModelContractTest {
	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun publicActions_loadContent_updateFilters_andEmitNavigationEffect() = runTest {
		val viewModel = createViewModel()
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

	private fun createViewModel(): EvaluationsViewModel {
		val repository = RecordingEvaluationRepository(
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT)
		)
		val textProvider = FakeEvaluationTextProvider()

		return EvaluationsViewModel(
			loadEvaluationsActionProcessor = LoadEvaluationsActionProcessor(
				getEvaluationsUseCase = GetEvaluationsUseCase(
					evaluationRepository = repository,
					exceptionHandler = GetEvaluationsExceptionHandler(
						reportingRepository = RecordingReportingRepository()
					)
				),
				filterLabelsProvider = FakeEvaluationFilterLabelsProvider(),
				textProvider = textProvider
			),
			checkEvaluationFilterActionProcessor = CheckEvaluationFilterActionProcessor(),
			uncheckEvaluationFilterActionProcessor = UncheckEvaluationFilterActionProcessor(),
			clearEvaluationFiltersActionProcessor = ClearEvaluationFiltersActionProcessor(),
			openAddEvaluationActionProcessor = OpenAddEvaluationActionProcessor(),
			pickEvaluationGradeActionProcessor = PickEvaluationGradeActionProcessor(
				getEvaluationUseCase = GetEvaluationUseCase(repository),
				textProvider = textProvider
			),
			setEvaluationGradeActionProcessor = SetEvaluationGradeActionProcessor(
				updateEvaluationUseCase = UpdateEvaluationUseCase(repository),
				textProvider = textProvider
			),
			openEvaluationActionProcessor = OpenEvaluationActionProcessor(),
			removeEvaluationActionProcessor = RemoveEvaluationActionProcessor(
				removeEvaluationUseCase = RemoveEvaluationUseCase(repository),
				textProvider = textProvider
			)
		)
	}
}
