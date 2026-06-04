package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationCourseFilter
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.RemoveEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationsExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.*
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsTab
import com.gdavidpb.tuindice.evaluations.testing.*
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
		val filter = EvaluationCourseFilter(DEFAULT_EVALUATION_SUBJECT.code)

		try {
			viewModel.state.test {
				assertEquals(Evaluations.State.Idle, awaitItem())

				viewModel.loadEvaluationsAction()
				val content = assertIs<Evaluations.State.Content>(awaitItem())
				assertEquals(EvaluationsTab.Upcoming, content.selectedTab)
				assertEquals(1, content.evaluationGroups.flatMap { group -> group.items }.size)
				assertEquals(1, content.historyGroups.flatMap { group -> group.items }.size)

				viewModel.selectTabAction(EvaluationsTab.History)
				val history = assertIs<Evaluations.State.Content>(awaitItem())
				assertEquals(EvaluationsTab.History, history.selectedTab)
				assertEquals(1, history.evaluationGroups.flatMap { group -> group.items }.size)

				viewModel.toggleFilterAction(filter, isChecked = true)
				val filtered = assertIs<Evaluations.State.Content>(awaitItem())
				assertEquals(listOf(filter), filtered.activeFilters)
				assertEquals(
					listOf(filter),
					filtered.filterGroups.flatMap { group -> group.items }
						.filter { item -> item.isChecked }
						.map { item -> item.filter }
				)

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
					DEFAULT_PENDING_EVALUATION,
					DEFAULT_COMPLETED_EVALUATION
				)
			),
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT)
		)

		return EvaluationsViewModel(
			loadEvaluationsActionProcessor = LoadEvaluationsActionProcessor(
					getEvaluationsUseCase = GetEvaluationsUseCase(
						evaluationRepository = repository,
						recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
						reportingRepository = RecordingReportingRepository()
					)
				),
			refreshEvaluationsActionProcessor = RefreshEvaluationsActionProcessor(
				updateEvaluationsUseCase = UpdateEvaluationsUseCase(
					evaluationRepository = repository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = UpdateEvaluationsExceptionHandler()
				)
			),
			checkEvaluationFilterActionProcessor = CheckEvaluationFilterActionProcessor(),
			uncheckEvaluationFilterActionProcessor = UncheckEvaluationFilterActionProcessor(),
			clearEvaluationFiltersActionProcessor = ClearEvaluationFiltersActionProcessor(),
			selectEvaluationsTabActionProcessor = SelectEvaluationsTabActionProcessor(),
			selectEvaluationsWeekActionProcessor = SelectEvaluationsWeekActionProcessor(),
			openAddEvaluationActionProcessor = OpenAddEvaluationActionProcessor(),
			pickEvaluationGradeActionProcessor = PickEvaluationGradeActionProcessor(
				getEvaluationUseCase = GetEvaluationUseCase(
					evaluationRepository = repository,
					reportingRepository = RecordingReportingRepository()
				)
			),
			setEvaluationGradeActionProcessor = SetEvaluationGradeActionProcessor(
				updateEvaluationUseCase = UpdateEvaluationUseCase(
					evaluationRepository = repository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = UpdateEvaluationExceptionHandler()
				)
			),
			openEvaluationActionProcessor = OpenEvaluationActionProcessor(),
			removeEvaluationActionProcessor = RemoveEvaluationActionProcessor(
				removeEvaluationUseCase = RemoveEvaluationUseCase(
					evaluationRepository = repository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = RemoveEvaluationExceptionHandler()
				)
			),
			eventPublisher = NoOpEventPublisher
		)
	}
}
