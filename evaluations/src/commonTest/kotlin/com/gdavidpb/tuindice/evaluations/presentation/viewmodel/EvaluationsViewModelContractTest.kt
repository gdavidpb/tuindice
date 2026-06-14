package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.RemoveEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationsMachine
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.testing.*
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class EvaluationsViewModelContractTest {
	@Test
	fun initialActionLoadsContent_andPublicActionsEmitNavigationEffect() = runTest {
		val viewModel = createViewModel(testScheduler)

		viewModel.state.test {
			assertEquals(Evaluations.State.Idle, awaitItem())

			val content = assertIs<Evaluations.State.Content>(awaitItem())
			assertEquals(2, content.evaluationGroups.flatMap { group -> group.items }.size)
			assertEquals(
				2,
				content.evaluationWeekGroups
					.flatMap { weekGroup -> weekGroup.groups }
					.flatMap { group -> group.items }
					.size
			)

			cancelAndIgnoreRemainingEvents()
		}

		viewModel.effect.test {
			viewModel.addEvaluationAction()
			assertIs<Evaluations.Effect.NavigateToAddEvaluation>(awaitItem())

			cancelAndIgnoreRemainingEvents()
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
			screenMachine = EvaluationsMachine(
				getEvaluationsUseCase = GetEvaluationsUseCase(
					evaluationRepository = repository,
					recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
					reportingRepository = RecordingReportingRepository()
				),
				updateEvaluationsUseCase = UpdateEvaluationsUseCase(
					evaluationRepository = repository,
					reportingRepository = RecordingReportingRepository()
				),
				getEvaluationUseCase = GetEvaluationUseCase(
					evaluationRepository = repository,
					reportingRepository = RecordingReportingRepository()
				),
				updateEvaluationUseCase = UpdateEvaluationUseCase(
					evaluationRepository = repository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = UpdateEvaluationExceptionHandler()
				),
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
