@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.data.source.InMemoryEvaluationsWeekFocusDataSource
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
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
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.fail
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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
	fun observedEvaluationWhoseSubjectIsGone_isLeftOutInsteadOfClosingTheScreen() = runTest {
		val orphan = DEFAULT_COMPLETED_EVALUATION.copy(
			id = "orphan-evaluation",
			attemptId = "attempt-that-is-gone"
		)
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			repository = RecordingEvaluationRepository(
				initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION, orphan),
				availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT)
			)
		)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				val content = awaitUntilState<Evaluations.State.Content>()

				assertEquals(
					listOf(DEFAULT_PENDING_EVALUATION.id),
					content.evaluationWeekGroups
						.flatMap { weekGroup -> weekGroup.groups }
						.flatMap { group -> group.items }
						.map { item -> item.evaluationId }
				)
				// The week strip describes the same list: the only dot is the listed evaluation's.
				assertEquals(
					1,
					content.weekItems
						.flatMap { weekItem -> weekItem.days }
						.count { day -> day.hasEvaluations }
				)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	// Evaluations left with no subject to be listed under are not content: once the refresh
	// confirms there is nothing else, the screen is the empty one, with its invitation to add the
	// first evaluation, never a list with nothing in it.
	@Test
	fun observedEvaluationsWhoseSubjectsAreAllGone_reachEmpty() = runTest {
		val orphans = listOf(
			DEFAULT_PENDING_EVALUATION.copy(attemptId = "gone-1"),
			DEFAULT_COMPLETED_EVALUATION.copy(
				attemptId = "gone-2",
				scheduleMode = EvaluationScheduleMode.CONTINUOUS
			)
		)
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			repository = RecordingEvaluationRepository(
				initialEvaluations = orphans,
				refreshedEvaluations = orphans,
				availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT)
			)
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
	}

	// The only subject of the list is numeric and has no meetings; the term still has a schedule
	// (a pass/fail subject meets), which is what the sheet shows.
	@Test
	fun empty_whenTheRefreshConfirmsIt_offersTheScheduleOfTheTerm() = runTest {
		val empty = confirmedEmpty(termHasSchedule = true)

		assertEquals(true, empty.hasSchedule)
		assertEquals(TopBarConfig.EvaluationsWithSchedule, empty.topBarConfig)
	}

	@Test
	fun empty_whenTheRefreshConfirmsItWithoutASchedule_offersNothing() = runTest {
		val empty = confirmedEmpty(termHasSchedule = false)

		assertEquals(false, empty.hasSchedule)
		assertEquals(null, empty.topBarConfig)
	}

	@Test
	fun empty_followsTheScheduleOfTheTerm_asItIsObserved() = runTest {
		val repository = RecordingEvaluationRepository(
			initialEvaluations = emptyList(),
			refreshedEvaluations = emptyList(),
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT),
			hasSchedule = false
		)
		val viewModel = createViewModel(testScheduler = testScheduler, repository = repository)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				viewModel.loadEvaluationsAction()
				viewModel.ensureEvaluationsLoadedAction()
				advanceUntilIdle()

				assertEquals(null, awaitUntilState<Evaluations.State.Empty>().topBarConfig)

				repository.changeHasSchedule(true)
				val withSchedule = awaitUntilState<Evaluations.State.Empty> { state -> state.hasSchedule }

				assertEquals(TopBarConfig.EvaluationsWithSchedule, withSchedule.topBarConfig)

				repository.changeHasSchedule(false)

				assertEquals(null, awaitUntilState<Evaluations.State.Empty> { state -> !state.hasSchedule }.topBarConfig)
				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	private suspend fun TestScope.confirmedEmpty(termHasSchedule: Boolean): Evaluations.State.Empty {
		val repository = RecordingEvaluationRepository(
			initialEvaluations = emptyList(),
			refreshedEvaluations = emptyList(),
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT),
			hasSchedule = termHasSchedule
		)
		val viewModel = createViewModel(testScheduler = testScheduler, repository = repository)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			var empty: Evaluations.State.Empty? = null

			viewModel.state.test {
				viewModel.ensureEvaluationsLoadedAction()
				advanceUntilIdle()

				empty = awaitUntilState<Evaluations.State.Empty>()
				cancelAndIgnoreRemainingEvents()
			}

			return checkNotNull(empty)
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

						is Evaluations.State.Empty -> fail("Refresh with content must not reduce through Empty")
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

						is Evaluations.State.Empty -> fail("No attempts must not reduce through Empty")
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
		firstViewModel.loadEvaluationsAction()
		val content = awaitUntilState<Evaluations.State.Content>()

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
		secondViewModel.loadEvaluationsAction()
		val restored = awaitUntilState<Evaluations.State.Content>()
		assertEquals(persistedWeekKey, restored.selectedWeekKey)

		cancelAndIgnoreRemainingEvents()
	}
}

@Test
fun selectWeek_persistsAcademicWeeksWithTheAnchorOfTheTerm() = runTest {
	val selectionRepository = InMemoryEvaluationsSelectionRepository()
	val viewModel = createViewModel(
		testScheduler = testScheduler,
		repository = sepDecRepository(classesStartDate = SEP_DEC_CLASSES_START),
		selectionRepository = selectionRepository,
		clock = FixedClock
	)

	viewModel.state.test {
		viewModel.loadEvaluationsAction()
		awaitUntilState<Evaluations.State.Content>()

		viewModel.selectWeekAction(EvaluationsWeekKey.Academic(5))
		assertEquals(EvaluationsWeekKey.Academic(5), assertIs<Evaluations.State.Content>(awaitItem()).selectedWeekKey)
		advanceUntilIdle()
		cancelAndIgnoreRemainingEvents()
	}

	assertEquals("academic:2026-09-28:5", selectionRepository.observeSelectedWeekKey().first())
}

@Test
fun savedWeek_ofTheSameAnchor_winsOverTheCurrentWeek() = runTest {
	val viewModel = createViewModel(
		testScheduler = testScheduler,
		repository = sepDecRepository(classesStartDate = SEP_DEC_CLASSES_START),
		selectionRepository = InMemoryEvaluationsSelectionRepository(initialWeekKey = "academic:2026-09-28:5"),
		clock = FixedClock
	)

	viewModel.state.test {
		viewModel.loadEvaluationsAction()
		val content = awaitUntilState<Evaluations.State.Content>()

		assertEquals(EvaluationsWeekKey.Academic(5), content.selectedWeekKey)
		cancelAndIgnoreRemainingEvents()
	}
}

@Test
fun savedWeek_ofAnotherAnchor_isDroppedForTheCurrentWeek() = runTest {
	// Saved when week 1 opened on August 31st: week 7 meant October 12th then, and November 9th now.
	val viewModel = createViewModel(
		testScheduler = testScheduler,
		repository = sepDecRepository(classesStartDate = SEP_DEC_CLASSES_START),
		selectionRepository = InMemoryEvaluationsSelectionRepository(initialWeekKey = "academic:2026-08-31:7"),
		clock = FixedClock
	)

	viewModel.state.test {
		viewModel.loadEvaluationsAction()
		val content = awaitUntilState<Evaluations.State.Content>()

		// October 14th is in week 3 counting from September 28th.
		assertEquals(EvaluationsWeekKey.Academic(3), content.selectedWeekKey)
		cancelAndIgnoreRemainingEvents()
	}
}

@Test
fun savedWeek_inTheOldFormatWithoutAnAnchor_isDroppedForTheCurrentWeek() = runTest {
	val viewModel = createViewModel(
		testScheduler = testScheduler,
		repository = sepDecRepository(classesStartDate = SEP_DEC_CLASSES_START),
		selectionRepository = InMemoryEvaluationsSelectionRepository(initialWeekKey = "academic:7"),
		clock = FixedClock
	)

	viewModel.state.test {
		viewModel.loadEvaluationsAction()
		val content = awaitUntilState<Evaluations.State.Content>()

		assertEquals(EvaluationsWeekKey.Academic(3), content.selectedWeekKey)
		cancelAndIgnoreRemainingEvents()
	}
}

@Test
fun savedContinuous_isKeptWhateverTheAnchor() = runTest {
	val continuous = DEFAULT_PENDING_EVALUATION.copy(
		id = "evaluation-continuous",
		scheduleMode = EvaluationScheduleMode.CONTINUOUS,
		date = null
	)
	val viewModel = createViewModel(
		testScheduler = testScheduler,
		repository = sepDecRepository(
			classesStartDate = SEP_DEC_CLASSES_START,
			evaluations = listOf(DEFAULT_PENDING_EVALUATION, continuous)
		),
		selectionRepository = InMemoryEvaluationsSelectionRepository(initialWeekKey = "continuous"),
		clock = FixedClock
	)

	viewModel.state.test {
		viewModel.loadEvaluationsAction()
		val content = awaitUntilState<Evaluations.State.Content>()

		assertEquals(EvaluationsWeekKey.Continuous, content.selectedWeekKey)
		cancelAndIgnoreRemainingEvents()
	}
}

@Test
fun whenTheFirstDayOfClassesArrivesWithTheTabOpen_aNumberedWeekIsDroppedForTheCurrentOne() = runTest {
	val repository = sepDecRepository(classesStartDate = null)
	val viewModel = createViewModel(
		testScheduler = testScheduler,
		repository = repository,
		clock = FixedClock
	)

	viewModel.state.test {
		viewModel.loadEvaluationsAction()
		val before = awaitUntilState<Evaluations.State.Content>()

		// Counting from August 31st, October 14th is week 7.
		assertEquals(EvaluationsWeekKey.Academic(7), before.selectedWeekKey)

		viewModel.selectWeekAction(EvaluationsWeekKey.Academic(5))
		assertEquals(EvaluationsWeekKey.Academic(5), assertIs<Evaluations.State.Content>(awaitItem()).selectedWeekKey)

		// A sync stores the date of the first day of classes: week 5 now means other dates.
		repository.changeCurrentTerm(sepDecTerm(classesStartDate = SEP_DEC_CLASSES_START))
		advanceUntilIdle()

		val after = awaitUntilState<Evaluations.State.Content> { content ->
			content.weekOneStart == SEP_DEC_CLASSES_START
		}

		assertEquals(EvaluationsWeekKey.Academic(3), after.selectedWeekKey)
		cancelAndIgnoreRemainingEvents()
	}
}

// Each LoadEvaluations (the retry button sends one) used to add another collector of the evaluations.
@Test
fun loadingAgain_leavesASingleCollectorOfEvaluations() = runTest {
	val collectors = MutableStateFlow(0)
	val repository = RecordingEvaluationRepository(
		evaluationsSnapshotFlow = flow {
			collectors.update { count -> count + 1 }
			try {
				awaitCancellation()
			} finally {
				collectors.update { count -> count - 1 }
			}
		},
		availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT)
	)
	val viewModel = createViewModel(testScheduler = testScheduler, repository = repository)

	viewModel.state.test {
		repeat(3) { viewModel.loadEvaluationsAction() }
		advanceUntilIdle()

		assertEquals(1, collectors.value)
		cancelAndIgnoreRemainingEvents()
	}
}

private fun sepDecTerm(classesStartDate: LocalDate?) = DEFAULT_EVALUATION_TERM.copy(
	periodCode = AcademicTermPeriod.SEP_DEC,
	classesStartDate = classesStartDate
)

private fun sepDecRepository(
	classesStartDate: LocalDate?,
	evaluations: List<Evaluation> = listOf(DEFAULT_PENDING_EVALUATION, DEFAULT_COMPLETED_EVALUATION)
) = RecordingEvaluationRepository(
	evaluationsFlow = kotlinx.coroutines.flow.flowOf(evaluations),
	initialEvaluations = evaluations,
	availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT),
	currentTerm = sepDecTerm(classesStartDate)
)

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
		selectionRepository: EvaluationsSelectionRepository = InMemoryEvaluationsSelectionRepository(),
		clock: Clock = Clock.System
	): EvaluationsViewModel {
		return EvaluationsViewModel(
			screenMachine = EvaluationsMachine(
				getEvaluationsUseCase = GetEvaluationsUseCase(
					evaluationRepository = repository,
					recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
					syncStatusRepository = RecordingSyncStatusRepository(),
					evaluationsSelectionRepository = selectionRepository,
					reportingRepository = RecordingReportingRepository(),
					clock = clock
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
					),
				weekFocusRepository = InMemoryEvaluationsWeekFocusDataSource(),
				clock = clock
			),
			eventPublisher = NoOpEventPublisher,
			dispatchers = TestTuIndiceDispatchers(UnconfinedTestDispatcher(testScheduler))
		)
	}

	private companion object {
		val SEP_DEC_CLASSES_START = LocalDate(2026, 9, 28)

		// October 14th, 2026, at noon UTC: a Wednesday of week 3 counting from September 28th and of
		// week 7 counting from August 31st, in every time zone the app is used in.
		val FixedClock = object : Clock {
			override fun now() = Instant.parse("2026-10-14T12:00:00Z")
		}
	}
}
