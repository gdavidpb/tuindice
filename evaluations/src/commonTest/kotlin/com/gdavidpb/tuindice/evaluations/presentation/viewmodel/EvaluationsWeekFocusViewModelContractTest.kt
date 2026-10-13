@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
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
		// It is a choice of the week like any other: it is what the next entry opens on.
		assertEquals("academic:2026-09-28:5", selectionRepository.observeSelectedWeekKey().first())
	}

	@Test
	fun weekFocus_onAMondayOutsideTheWeeks_isConsumedWithoutMovingTheSelection() = runTest {
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
			listOf(LocalDate(2026, 9, 21), LocalDate(2026, 12, 21)).forEach { monday ->
				weekFocus.request(monday)
				advanceUntilIdle()

				assertNull(weekFocus.pendingWeekStart.value, "left pending: $monday")
			}

			cancelAndIgnoreRemainingEvents()
		}

		val content = assertIs<Evaluations.State.Content>(viewModel.state.value)
		assertEquals(EvaluationsWeekKey.Academic(3), content.selectedWeekKey)
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

	private fun repository(
		availableSubjects: List<EditableAttemptDescriptor> =
			listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT),
		hasEvaluations: Boolean = true
	): RecordingEvaluationRepository {
		val evaluations = if (hasEvaluations) {
			listOf(DEFAULT_PENDING_EVALUATION, DEFAULT_COMPLETED_EVALUATION)
		} else {
			emptyList()
		}

		return RecordingEvaluationRepository(
			evaluationsFlow = flowOf(evaluations),
			initialEvaluations = evaluations,
			availableSubjects = availableSubjects,
			currentTerm = DEFAULT_EVALUATION_TERM.copy(
				periodCode = AcademicTermPeriod.SEP_DEC,
				classesStartDate = CLASSES_START
			)
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
