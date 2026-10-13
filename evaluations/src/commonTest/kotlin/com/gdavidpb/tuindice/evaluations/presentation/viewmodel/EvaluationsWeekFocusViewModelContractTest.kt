@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.base.data.source.InMemoryEvaluationsWeekFocusDataSource
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.repository.EvaluationsWeekFocusRepository
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
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
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_COMPLETED_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_TERM
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.InMemoryEvaluationsSelectionRepository
import com.gdavidpb.tuindice.evaluations.testing.ReadyRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingSyncStatusRepository
import com.gdavidpb.tuindice.evaluations.testing.SECOND_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import com.gdavidpb.tuindice.testkit.mvi.awaitUntilState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * The request to show a week of the tab (see EvaluationsWeekFocusRepository) as the view model
 * lives it: asked before the tab exists, asked with its content up, outside the weeks, and applied once.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class EvaluationsWeekFocusViewModelContractTest {
	@Test
	fun weekFocus_askedBeforeTheScreenExists_isAppliedWhenTheContentLoads() = runTest {
		val weekFocus = InMemoryEvaluationsWeekFocusDataSource()

		// The tab had not been composed when the schedule asked for week 5 (it opens on Monday October 26th).
		weekFocus.request(WEEK_FIVE_START)

		val viewModel = createViewModel(
			testScheduler = testScheduler,
			repository = repository(),
			weekFocusRepository = weekFocus
		)

		viewModel.state.test {
			viewModel.loadEvaluationsAction()
			val content = awaitUntilState<Evaluations.State.Content> { state ->
				state.selectedWeekKey == EvaluationsWeekKey.Academic(5)
			}

			assertEquals(EvaluationsWeekKey.Academic(5), content.weekItem.key)
			advanceUntilIdle()
			cancelAndIgnoreRemainingEvents()
		}

		assertNull(weekFocus.pendingWeekStart.value)
	}

	@Test
	fun weekFocus_askedWithTheContentLoaded_selectsThatWeekAndIsConsumed() = runTest {
		val weekFocus = InMemoryEvaluationsWeekFocusDataSource()
		val selectionRepository = InMemoryEvaluationsSelectionRepository()
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			repository = repository(),
			selectionRepository = selectionRepository,
			weekFocusRepository = weekFocus
		)

		viewModel.state.test {
			viewModel.loadEvaluationsAction()
			val before = awaitUntilState<Evaluations.State.Content>()

			assertEquals(EvaluationsWeekKey.Academic(3), before.selectedWeekKey)

			weekFocus.request(WEEK_FIVE_START)
			awaitUntilState<Evaluations.State.Content> { state -> state.selectedWeekKey == EvaluationsWeekKey.Academic(5) }
			advanceUntilIdle()
			cancelAndIgnoreRemainingEvents()
		}

		assertNull(weekFocus.pendingWeekStart.value)
		// A jump is navigation, not a preference: the next entry follows the current week again.
		assertNull(selectionRepository.observeSelectedWeekKey().first())
	}

	@Test
	fun weekFocus_onAMondayOutsideTheWeeks_selectsTheFirstOrTheLastWeekLikeTheListFilesIt() = runTest {
		val weekFocus = InMemoryEvaluationsWeekFocusDataSource()
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			repository = repository(),
			weekFocusRepository = weekFocus
		)

		viewModel.state.test {
			viewModel.loadEvaluationsAction()
			awaitUntilState<Evaluations.State.Content>()

			// Before week 1 and after week 12 (week 12 opens on Monday December 14th).
			weekFocus.request(LocalDate(2026, 9, 21))
			awaitUntilState<Evaluations.State.Content> { state -> state.selectedWeekKey == EvaluationsWeekKey.Academic(1) }

			weekFocus.request(LocalDate(2026, 12, 21))
			awaitUntilState<Evaluations.State.Content> { state -> state.selectedWeekKey == EvaluationsWeekKey.Academic(12) }
			advanceUntilIdle()
			cancelAndIgnoreRemainingEvents()
		}

		assertNull(weekFocus.pendingWeekStart.value)
	}

	// Today is week 3 and the user had chosen week 10; the schedule asks for week 13, where an
	// evaluation is marked. The list files it under week 12, so that is where the jump has to land.
	@Test
	fun weekFocus_onWeekThirteen_landsWhereTheMarkedEvaluationIsListed() = runTest {
		val weekFocus = InMemoryEvaluationsWeekFocusDataSource()
		val evaluation = DEFAULT_PENDING_EVALUATION.copy(date = DEC_22_NOON_MILLIS)
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			repository = repository(evaluations = listOf(evaluation)),
			selectionRepository = InMemoryEvaluationsSelectionRepository(initialWeekKey = "academic:2026-09-28:10"),
			weekFocusRepository = weekFocus
		)

		viewModel.state.test {
			viewModel.loadEvaluationsAction()
			val before = awaitUntilState<Evaluations.State.Content>()

			assertEquals(EvaluationsWeekKey.Academic(10), before.selectedWeekKey)

			weekFocus.request(LocalDate(2026, 12, 21))
			awaitUntilState<Evaluations.State.Content> { state -> state.selectedWeekKey == EvaluationsWeekKey.Academic(12) }
			advanceUntilIdle()
			cancelAndIgnoreRemainingEvents()
		}

		val content = assertIs<Evaluations.State.Content>(viewModel.state.value)
		assertEquals(listOf<EvaluationsWeekKey>(EvaluationsWeekKey.Academic(12)), content.evaluationWeekGroups.map { it.key })
		assertEquals(content.selectedWeekKey, content.evaluationWeekGroups.single().key)
	}

	@Test
	fun weekFocus_isNotSavedButASelectionIs() = runTest {
		val weekFocus = InMemoryEvaluationsWeekFocusDataSource()
		val selectionRepository = InMemoryEvaluationsSelectionRepository()
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			repository = repository(),
			selectionRepository = selectionRepository,
			weekFocusRepository = weekFocus
		)

		viewModel.state.test {
			viewModel.loadEvaluationsAction()
			awaitUntilState<Evaluations.State.Content>()

			weekFocus.request(WEEK_FIVE_START)
			awaitUntilState<Evaluations.State.Content> { state -> state.selectedWeekKey == EvaluationsWeekKey.Academic(5) }
			advanceUntilIdle()

			assertNull(selectionRepository.observeSelectedWeekKey().first())

			viewModel.selectWeekAction(EvaluationsWeekKey.Academic(2))
			awaitUntilState<Evaluations.State.Content> { state -> state.selectedWeekKey == EvaluationsWeekKey.Academic(2) }
			advanceUntilIdle()

			assertEquals("academic:2026-09-28:2", selectionRepository.observeSelectedWeekKey().first())
			cancelAndIgnoreRemainingEvents()
		}
	}

	// Each LoadEvaluations (the retry button sends one) used to start another collector of the focus.
	@Test
	fun loadingAgain_leavesASingleCollectorOfTheWeekFocus() = runTest {
		val pending = MutableStateFlow<LocalDate?>(null)
		val weekFocus = object : EvaluationsWeekFocusRepository {
			override val pendingWeekStart: StateFlow<LocalDate?> = pending

			override fun request(weekStart: LocalDate) {
				pending.value = weekStart
			}

			override fun consume(weekStart: LocalDate) = pending.compareAndSet(weekStart, null)
		}
		val viewModel = createViewModel(testScheduler = testScheduler, weekFocusRepository = weekFocus)

		viewModel.state.test {
			awaitUntilState<Evaluations.State.Content>()
			advanceUntilIdle()

			repeat(3) { viewModel.loadEvaluationsAction() }
			advanceUntilIdle()

			assertEquals(1, pending.subscriptionCount.value)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun weekFocus_isAppliedOnce_andALaterChoiceOfTheUserIsNotUndone() = runTest {
		val weekFocus = InMemoryEvaluationsWeekFocusDataSource()
		val repository = repository()
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			repository = repository,
			weekFocusRepository = weekFocus
		)

		viewModel.state.test {
			viewModel.loadEvaluationsAction()
			awaitUntilState<Evaluations.State.Content>()

			weekFocus.request(WEEK_FIVE_START)
			awaitUntilState<Evaluations.State.Content> { state -> state.selectedWeekKey == EvaluationsWeekKey.Academic(5) }

			viewModel.selectWeekAction(EvaluationsWeekKey.Academic(2))
			awaitUntilState<Evaluations.State.Content> { state -> state.selectedWeekKey == EvaluationsWeekKey.Academic(2) }

			// The content is observed again (a sync, coming back to the tab): nothing is pending to apply.
			viewModel.loadEvaluationsAction()
			advanceUntilIdle()

			assertEquals(
				EvaluationsWeekKey.Academic(2),
				assertIs<Evaluations.State.Content>(viewModel.state.value).selectedWeekKey
			)
			assertFalse(weekFocus.consume(WEEK_FIVE_START))
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun weekFocus_whenTheScreenSettlesOnNothingToShow_isDroppedInsteadOfWaitingForever() = runTest {
		val weekFocus = InMemoryEvaluationsWeekFocusDataSource()

		weekFocus.request(WEEK_FIVE_START)

		val viewModel = createViewModel(
			testScheduler = testScheduler,
			repository = repository(availableSubjects = emptyList(), hasEvaluations = false),
			weekFocusRepository = weekFocus
		)

		viewModel.state.test {
			viewModel.loadEvaluationsAction()
			awaitUntilState<Evaluations.State> { state ->
				state !is Evaluations.State.Idle && state !is Evaluations.State.Loading
			}
			advanceUntilIdle()
			cancelAndIgnoreRemainingEvents()
		}

		assertNull(weekFocus.pendingWeekStart.value)
	}

	@Test
	fun topBar_offersTheScheduleOnlyWhenSomeSubjectOfTheTermHasMeetings() = runTest {
		val withMeetings = createViewModel(
			testScheduler = testScheduler,
			repository = repository(
				availableSubjects = listOf(
					DEFAULT_EVALUATION_SUBJECT.copy(meetings = listOf(CLASS_MEETING)),
					SECOND_EVALUATION_SUBJECT
				)
			)
		)
		val withoutMeetings = createViewModel(
			testScheduler = testScheduler,
			repository = repository()
		)

		withMeetings.state.test {
			withMeetings.loadEvaluationsAction()
			val content = awaitUntilState<Evaluations.State.Content>()

			assertEquals(TopBarConfig.EvaluationsWithSchedule, content.topBarConfig)
			cancelAndIgnoreRemainingEvents()
		}

		withoutMeetings.state.test {
			withoutMeetings.loadEvaluationsAction()
			val content = awaitUntilState<Evaluations.State.Content>()

			assertNull(content.topBarConfig)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun topBar_offersTheScheduleWhenTheOnlyMeetingsAreOnSubjectsTheListDoesNotOffer() = runTest {
		// The numeric subjects of the list have no meetings; a pass/fail subject of the term does.
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			repository = repository(termHasSchedule = true)
		)

		viewModel.state.test {
			viewModel.loadEvaluationsAction()
			val content = awaitUntilState<Evaluations.State.Content>()

			assertEquals(TopBarConfig.EvaluationsWithSchedule, content.topBarConfig)
			cancelAndIgnoreRemainingEvents()
		}
	}

	private fun repository(
		availableSubjects: List<EditableAttemptDescriptor> =
			listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT),
		hasEvaluations: Boolean = true,
		termHasSchedule: Boolean = availableSubjects.any { subject -> subject.meetings.isNotEmpty() },
		evaluations: List<Evaluation> = if (hasEvaluations) {
			listOf(DEFAULT_PENDING_EVALUATION, DEFAULT_COMPLETED_EVALUATION)
		} else {
			emptyList()
		}
	): RecordingEvaluationRepository {
		return RecordingEvaluationRepository(
			evaluationsFlow = flowOf(evaluations),
			initialEvaluations = evaluations,
			availableSubjects = availableSubjects,
			currentTerm = DEFAULT_EVALUATION_TERM.copy(
				periodCode = AcademicTermPeriod.SEP_DEC,
				classesStartDate = CLASSES_START
			),
			hasSchedule = termHasSchedule
		)
	}

	private fun createViewModel(
		testScheduler: TestCoroutineScheduler,
		repository: RecordingEvaluationRepository = repository(),
		selectionRepository: InMemoryEvaluationsSelectionRepository = InMemoryEvaluationsSelectionRepository(),
		weekFocusRepository: EvaluationsWeekFocusRepository = InMemoryEvaluationsWeekFocusDataSource()
	): EvaluationsViewModel {
		return EvaluationsViewModel(
			screenMachine = EvaluationsMachine(
				getEvaluationsUseCase = GetEvaluationsUseCase(
					evaluationRepository = repository,
					recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
					syncStatusRepository = RecordingSyncStatusRepository(),
					evaluationsSelectionRepository = selectionRepository,
					reportingRepository = RecordingReportingRepository(),
					clock = FixedClock
				),
				ensureEvaluationsLoadedUseCase = EnsureEvaluationsLoadedUseCase(
					repository,
					RecordingReportingRepository()
				),
				updateEvaluationsUseCase = UpdateEvaluationsUseCase(repository, RecordingReportingRepository()),
				getEvaluationUseCase = GetEvaluationUseCase(repository, RecordingReportingRepository()),
				updateEvaluationUseCase = UpdateEvaluationUseCase(
					repository,
					RecordingReportingRepository(),
					UpdateEvaluationExceptionHandler()
				),
				removeEvaluationUseCase = RemoveEvaluationUseCase(
					repository,
					RecordingReportingRepository(),
					RemoveEvaluationExceptionHandler()
				),
				setSelectedWeekUseCase = SetSelectedWeekUseCase(selectionRepository, RecordingReportingRepository()),
				weekFocusRepository = weekFocusRepository,
				clock = FixedClock
			),
			eventPublisher = NoOpEventPublisher,
			dispatchers = TestTuIndiceDispatchers(UnconfinedTestDispatcher(testScheduler))
		)
	}

	private companion object {
		// Monday September 28th, 2026 opens week 1 of the term.
		val CLASSES_START = LocalDate(2026, 9, 28)

		// Tuesday December 22nd, 2026, at noon UTC: a day of week 13 of that term.
		val DEC_22_NOON_MILLIS = Instant.parse("2026-12-22T16:00:00Z").toEpochMilliseconds()

		// Week 5 of that term.
		val WEEK_FIVE_START = LocalDate(2026, 10, 26)

		val CLASS_MEETING = ScheduleMeeting(
			attemptId = DEFAULT_EVALUATION_SUBJECT.id,
			subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
			section = 1,
			dayOfWeek = 1,
			startBlock = 1,
			endBlock = 2,
			classroom = "MYS-116"
		)

		// October 14th, 2026, at noon UTC: a Wednesday of week 3 of that term.
		val FixedClock = object : Clock {
			override fun now() = Instant.parse("2026-10-14T12:00:00Z")
		}
	}
}
