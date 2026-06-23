package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.EnsureEvaluationsLoadedUseCase
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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.fail

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

	@Test
	fun ensureLoaded_whenLocalEmptyAndRefreshAddsContent_neverEmitsEmpty() = runTest {
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			repository = RecordingEvaluationRepository(
				initialEvaluations = emptyList(),
				refreshedEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
				availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT)
			)
		)

		viewModel.state.test {
			assertEquals(Evaluations.State.Idle, awaitItem())

			viewModel.ensureEvaluationsLoadedAction()

			assertIs<Evaluations.State.Loading>(awaitItem())
			assertIs<Evaluations.State.Content>(awaitItem())

			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun ensureLoaded_whenLocalEmptyAndRefreshStaysEmptyWithAttempts_confirmsEmpty() = runTest {
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			repository = RecordingEvaluationRepository(
				initialEvaluations = emptyList(),
				refreshedEvaluations = emptyList(),
				availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT)
			)
		)

		viewModel.state.test {
			assertEquals(Evaluations.State.Idle, awaitItem())

			viewModel.ensureEvaluationsLoadedAction()

			assertIs<Evaluations.State.Loading>(awaitItem())
			assertEquals(Evaluations.State.Empty, awaitItem())

			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun ensureLoaded_whenAttemptsAreUnavailable_endsInNoAttemptsNotEmpty() = runTest {
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			repository = RecordingEvaluationRepository(
				initialEvaluations = emptyList(),
				refreshedEvaluations = emptyList(),
				availableSubjects = emptyList()
			)
		)

		viewModel.state.test {
			assertEquals(Evaluations.State.Idle, awaitItem())
			viewModel.ensureEvaluationsLoadedAction()

			var reachedNoAttempts = false
			var observedStates = 0
			while (!reachedNoAttempts && observedStates < 4) {
				observedStates++
				when (val state = awaitItem()) {
					is Evaluations.State.NoAttempts -> reachedNoAttempts = true

					Evaluations.State.Empty -> fail("No attempts must not reduce through Empty")
					else -> assertFalse(state is Evaluations.State.Empty)
				}
			}

			if (!reachedNoAttempts) fail("Expected NoAttempts")
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun ensureLoaded_whenContentExists_keepsContentDuringInitialRefresh() = runTest {
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			repository = RecordingEvaluationRepository(
				initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
				refreshedEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
				availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT)
			)
		)

		viewModel.state.test {
			assertEquals(Evaluations.State.Idle, awaitItem())
			assertIs<Evaluations.State.Content>(awaitItem())

			viewModel.ensureEvaluationsLoadedAction()
			advanceUntilIdle()

			expectNoEvents()
			cancelAndIgnoreRemainingEvents()
		}
	}

	private fun createViewModel(
		testScheduler: TestCoroutineScheduler,
		repository: EvaluationRepository = RecordingEvaluationRepository(
			evaluationsFlow = kotlinx.coroutines.flow.flowOf(
				listOf(
					DEFAULT_PENDING_EVALUATION,
					DEFAULT_COMPLETED_EVALUATION
				)
			),
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT)
		)
	): EvaluationsViewModel {
		return EvaluationsViewModel(
			screenMachine = EvaluationsMachine(
				getEvaluationsUseCase = GetEvaluationsUseCase(
					evaluationRepository = repository,
					recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
					syncStatusRepository = RecordingSyncStatusRepository(),
					reportingRepository = RecordingReportingRepository()
				),
				ensureEvaluationsLoadedUseCase = EnsureEvaluationsLoadedUseCase(
					evaluationRepository = repository,
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
