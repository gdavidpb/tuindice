package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationsSelectionRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.EnsureEvaluationsLoadedUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.SetSelectedWeekUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.RemoveEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationsMachine
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.testing.*
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import com.gdavidpb.tuindice.testkit.mvi.awaitUntilState
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				val content = awaitUntilState<Evaluations.State.Content>()
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
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun ensureLoaded_whenLocalEmptyAndRefreshAddsContent_neverEmitsEmpty() = runTest {
		val repository = RecordingEvaluationRepository(
			initialEvaluations = emptyList(),
			refreshedEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT)
		)
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			repository = repository
		)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				viewModel.ensureEvaluationsLoadedAction()

				var reachedContent = false
				var observedStates = 0
				while (!reachedContent && observedStates < 4) {
					observedStates++
					when (val state = awaitItem()) {
						is Evaluations.State.Content -> {
							reachedContent = true
							assertEquals(1, state.evaluationGroups.flatMap { group -> group.items }.size)
						}

						Evaluations.State.Empty -> fail("Refresh with content must not reduce through Empty")
						else -> assertFalse(state is Evaluations.State.Empty)
					}
				}

				if (!reachedContent) fail("Expected Content")
				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}

		assertEquals(1, repository.updateEvaluationsCalls)
		assertEquals(listOf(true), repository.updateEvaluationsForceRemoteCalls)
	}

	@Test
	fun ensureLoaded_whenLocalEmptyAndRefreshStaysEmptyWithAttempts_confirmsEmpty() = runTest {
		val repository = RecordingEvaluationRepository(
			initialEvaluations = emptyList(),
			refreshedEvaluations = emptyList(),
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT)
		)
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			repository = repository
		)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				viewModel.ensureEvaluationsLoadedAction()
				advanceUntilIdle()

				awaitUntilState<Evaluations.State.Empty>()
				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}

		assertEquals(1, repository.updateEvaluationsCalls)
		assertEquals(listOf(true), repository.updateEvaluationsForceRemoteCalls)
	}

	@Test
	fun ensureLoaded_whenAttemptsAreUnavailable_endsInNoAttemptsNotEmpty() = runTest {
		val repository = RecordingEvaluationRepository(
			initialEvaluations = emptyList(),
			refreshedEvaluations = emptyList(),
			availableSubjects = emptyList()
		)
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			repository = repository
		)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
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
				advanceUntilIdle()
				expectNoEvents()
				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}

		assertEquals(1, repository.updateEvaluationsCalls)
		assertEquals(listOf(true), repository.updateEvaluationsForceRemoteCalls)
	}

	@Test
	fun ensureLoaded_whenContentExists_keepsContentDuringInitialRefresh() = runTest {
		val repository = RecordingEvaluationRepository(
			initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
			refreshedEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT)
		)
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			repository = repository
		)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				awaitUntilState<Evaluations.State.Content>()

				viewModel.ensureEvaluationsLoadedAction()
				advanceUntilIdle()

				expectNoEvents()
				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}

	assertEquals(1, repository.updateEvaluationsCalls)
	assertEquals(listOf(false), repository.updateEvaluationsForceRemoteCalls)
}

@Test
fun ensureLoaded_whenCacheExistsBeforeObservation_doesNotShowLoadingDuringInitialRefresh() = runTest {
	val observedSnapshots = MutableSharedFlow<ObservedSyncedSnapshot<List<Evaluation>>>()
	val cachedSnapshot = ObservedSyncedSnapshot(
		value = listOf(DEFAULT_PENDING_EVALUATION),
		hasSynced = true
	)
	val repository = RecordingEvaluationRepository(
		initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
		refreshedEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
		availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT),
		evaluationsSnapshotFlow = observedSnapshots,
		evaluationsSnapshot = cachedSnapshot
	)
	val viewModel = createViewModel(
		testScheduler = testScheduler,
		repository = repository
	)

	viewModel.state.test {
		assertEquals(Evaluations.State.Idle, awaitItem())

		viewModel.ensureEvaluationsLoadedAction()
		advanceUntilIdle()
		expectNoEvents()

		observedSnapshots.emit(cachedSnapshot)
		assertIs<Evaluations.State.Content>(awaitItem())
		cancelAndIgnoreRemainingEvents()
	}

	assertEquals(1, repository.updateEvaluationsCalls)
	assertEquals(listOf(false), repository.updateEvaluationsForceRemoteCalls)
}

@Test
fun ensureLoaded_whenCacheExistsAndInitialRefreshFails_doesNotShowFailedBeforeCachedContent() = runTest {
	val observedSnapshots = MutableSharedFlow<ObservedSyncedSnapshot<List<Evaluation>>>()
	val cachedSnapshot = ObservedSyncedSnapshot(
		value = listOf(DEFAULT_PENDING_EVALUATION),
		hasSynced = true
	)
	val repository = RecordingEvaluationRepository(
		initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
		availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT),
		refreshThrowable = RuntimeException("network unavailable"),
		evaluationsSnapshotFlow = observedSnapshots,
		evaluationsSnapshot = cachedSnapshot
	)
	val viewModel = createViewModel(
		testScheduler = testScheduler,
		repository = repository
	)

	viewModel.state.test {
		assertEquals(Evaluations.State.Idle, awaitItem())

		viewModel.ensureEvaluationsLoadedAction()
		advanceUntilIdle()
		expectNoEvents()

		observedSnapshots.emit(cachedSnapshot)
		assertIs<Evaluations.State.Content>(awaitItem())
		cancelAndIgnoreRemainingEvents()
	}

	assertEquals(1, repository.updateEvaluationsCalls)
	assertEquals(listOf(false), repository.updateEvaluationsForceRemoteCalls)
}

@Test
fun selectWeek_persistsSelectionAndRestoresItOnNextScreenEntry() = runTest {
	val selectionRepository = InMemoryEvaluationsSelectionRepository()
	val firstViewModel = createViewModel(
		testScheduler = testScheduler,
		selectionRepository = selectionRepository
	)

	lateinit var persistedWeekKey: EvaluationsWeekKey

	firstViewModel.state.test {
		assertEquals(Evaluations.State.Idle, awaitItem())

		firstViewModel.loadEvaluationsAction()
		val content = assertIs<Evaluations.State.Content>(awaitItem())

		// Precondición del escenario: debe existir una semana distinta a la seleccionada.
		persistedWeekKey = content.weekItems
			.first { item -> item.key != content.selectedWeekKey }
			.key

		firstViewModel.selectWeekAction(persistedWeekKey)
		val updated = assertIs<Evaluations.State.Content>(awaitItem())
		assertEquals(persistedWeekKey, updated.selectedWeekKey)

		advanceUntilIdle()
		cancelAndIgnoreRemainingEvents()
	}

	val secondViewModel = createViewModel(
		testScheduler = testScheduler,
		selectionRepository = selectionRepository
	)

	secondViewModel.state.test {
		assertEquals(Evaluations.State.Idle, awaitItem())

		secondViewModel.loadEvaluationsAction()
		val restored = assertIs<Evaluations.State.Content>(awaitItem())
		assertEquals(persistedWeekKey, restored.selectedWeekKey)

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
		),
		selectionRepository: EvaluationsSelectionRepository = InMemoryEvaluationsSelectionRepository()
	): EvaluationsViewModel {
		return EvaluationsViewModel(
			screenMachine = EvaluationsMachine(
				getEvaluationsUseCase = GetEvaluationsUseCase(
					evaluationRepository = repository,
					recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
					syncStatusRepository = RecordingSyncStatusRepository(),
					evaluationsSelectionRepository = selectionRepository,
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
					),
					setSelectedWeekUseCase = SetSelectedWeekUseCase(
						evaluationsSelectionRepository = selectionRepository,
						reportingRepository = RecordingReportingRepository()
					)
			),
			eventPublisher = NoOpEventPublisher,
			dispatchers = TestTuIndiceDispatchers(UnconfinedTestDispatcher(testScheduler))
		)
	}
}
