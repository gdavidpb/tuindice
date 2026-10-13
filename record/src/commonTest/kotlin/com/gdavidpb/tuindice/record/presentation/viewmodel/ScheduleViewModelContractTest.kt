package com.gdavidpb.tuindice.record.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.domain.repository.ScheduleClockRepository
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleEvaluationsRepository
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.domain.usecase.ObserveScheduleUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetScheduleViewModeUseCase
import com.gdavidpb.tuindice.record.presentation.contract.Schedule
import com.gdavidpb.tuindice.record.presentation.machine.ScheduleMachine
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.testing.ControllableAcademicRecordRepository
import com.gdavidpb.tuindice.record.testing.ControllableScheduleClockRepository
import com.gdavidpb.tuindice.record.testing.ControllableVisibleEvaluationsRepository
import com.gdavidpb.tuindice.record.testing.RecordingScheduleSelectionRepository
import com.gdavidpb.tuindice.record.testing.ScheduleTestDate
import com.gdavidpb.tuindice.record.testing.SleepingScheduleClockRepository
import com.gdavidpb.tuindice.record.testing.academicAttempt
import com.gdavidpb.tuindice.record.testing.academicTerm
import com.gdavidpb.tuindice.record.testing.scheduleEvaluation
import com.gdavidpb.tuindice.record.testing.scheduleNow
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.awaitUntilState
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.test.runTest
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_subtitle_week
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ScheduleViewModelContractTest {
	@Test
	fun observe_withAScheduledCurrentTerm_resolvesContentOnTheStoredView() = runTest {
		val fixture = createFixture(record = scheduledRecord(), hasSynced = true)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				val content = awaitUntilState<Schedule.State.Content>()

				assertEquals(ScheduleViewMode.Table, content.viewMode)
				assertEquals(listOf("CI5311"), content.schedule.table.rows.map { it.subjectCode })

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun observe_withAnEvaluationThisWeek_marksItsCell_andSaysTheWeekOfTheTerm() = runTest {
		val fixture = createFixture(
			record = mondayRecord(),
			hasSynced = true,
			visibleEvaluationsRepository = ControllableVisibleEvaluationsRepository(
				initialEvaluations = listOf(scheduleEvaluation(attemptId = "attempt-CI5311", date = ScheduleTestDate))
			)
		)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				val content = awaitUntilState<Schedule.State.Content>()
				val meeting = content.schedule.table.rows.single().meetings.getValue(ScheduleDay.Monday).single()

				assertEquals(ScheduleTestDate, meeting.evaluationWeekStart)
				assertTrue(content.schedule.table.hasEvaluationMarks)
				// Week 1 opens on Monday 2026-08-31: the Monday 2026-10-05 is week 6.
				assertEquals(
					UiText.Resource(Res.string.schedule_subtitle_week, listOf("Sep - Dic 2026", 6)),
					content.subtitle
				)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun observe_whenTheEvaluationsAppearLater_theCellIsMarkedWithoutReopeningTheSheet() = runTest {
		val evaluations = ControllableVisibleEvaluationsRepository()
		val fixture = createFixture(record = mondayRecord(), hasSynced = true, visibleEvaluationsRepository = evaluations)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				assertFalse(awaitUntilState<Schedule.State.Content>().schedule.table.hasEvaluationMarks)

				evaluations.snapshotFlow.value = ObservedSyncedSnapshot(
					value = listOf(scheduleEvaluation(attemptId = "attempt-CI5311", date = ScheduleTestDate)),
					hasSynced = true
				)

				val marked = awaitUntilState<Schedule.State.Content> { it.schedule.table.hasEvaluationMarks }

				assertTrue(marked.schedule.table.hasEvaluationMarks)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun observe_withTheEvaluationsFailing_theScheduleIsDrawnWithoutMarks() = runTest {
		val fixture = createFixture(
			record = mondayRecord(),
			hasSynced = true,
			visibleEvaluationsRepository = ControllableVisibleEvaluationsRepository(
				failure = IllegalStateException("local database is gone")
			)
		)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				val content = awaitUntilState<Schedule.State.Content>()

				assertEquals(listOf("CI5311"), content.schedule.table.rows.map { it.subjectCode })
				assertFalse(content.schedule.table.hasEvaluationMarks)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun evaluationCellSelected_asksToGoToTheEvaluationsOfThatWeek_andLeavesTheSheetAsItIs() = runTest {
		val fixture = createFixture(
			record = mondayRecord(),
			hasSynced = true,
			visibleEvaluationsRepository = ControllableVisibleEvaluationsRepository(
				initialEvaluations = listOf(scheduleEvaluation(attemptId = "attempt-CI5311", date = ScheduleTestDate))
			)
		)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			var before: Schedule.State.Content? = null

			fixture.viewModel.state.test {
				before = awaitUntilState<Schedule.State.Content>()

				cancelAndIgnoreRemainingEvents()
			}

			fixture.viewModel.effect.test(timeout = 15.seconds) {
				fixture.viewModel.evaluationCellSelectedAction(weekStart = ScheduleTestDate)

				val effect = assertIs<Schedule.Effect.NavigateToEvaluationsWeek>(awaitItem())

				assertEquals(ScheduleTestDate, effect.weekStart)

				cancelAndIgnoreRemainingEvents()
			}

			assertEquals<Schedule.State?>(before, fixture.viewModel.state.value)
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun selectView_persistsTheChoice_andContentFollowsIt() = runTest {
		val fixture = createFixture(record = scheduledRecord(), hasSynced = true)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				awaitUntilState<Schedule.State.Content>()

				fixture.viewModel.selectScheduleViewAction(ScheduleViewMode.Week)

				val content = awaitUntilState<Schedule.State.Content> { it.viewMode == ScheduleViewMode.Week }

				assertEquals(ScheduleViewMode.Week, content.viewMode)
				// The state follows optimistically; the write is a coroutine of its own, so wait for it.
				assertEquals(listOf(ScheduleViewMode.Week), fixture.selectionRepository.awaitViewModeCalls(count = 1))

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun observe_whenTheMinuteChanges_contentFollowsTheClock() = runTest {
		val fixture = createFixture(record = scheduledRecord(), hasSynced = true)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				// Monday 8:00: halfway through block 1 of the Monday meeting.
				val atEight = awaitUntilState<Schedule.State.Content>()

				assertEquals(ScheduleDay.Monday, atEight.schedule.table.today)
				assertEquals(0.5f, atEight.schedule.grid.days.first().nowBlockOffset)
				assertTrue(atEight.schedule.grid.days.first().cells.single().isInProgress)

				// 9:30: the meeting (blocks 1-2) is over, and so is the grid.
				(fixture.clockRepository as ControllableScheduleClockRepository).nowFlow.value =
					scheduleNow(dayOfWeek = 2, minuteOfDay = 9 * 60 + 30)

				val afterClass = awaitUntilState<Schedule.State.Content> { content ->
					content.schedule.grid.days.first().nowBlockOffset == null
				}

				assertEquals(ScheduleDay.Monday, afterClass.schedule.table.today)
				assertFalse(afterClass.schedule.grid.days.first().cells.single().isInProgress)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	// The ticker sleeps while the process is suspended and the value stays old until the next tick,
	// up to a minute. Coming back reads the clock again.
	@Test
	fun resume_readsTheClockAgain_insteadOfWaitingForTheNextTick() = runTest {
		val clock = SleepingScheduleClockRepository()
		val fixture = createFixture(record = scheduledRecord(), hasSynced = true, clockRepository = clock)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				// Monday 8:00: halfway through block 1 of the Monday meeting.
				val atEight = awaitUntilState<Schedule.State.Content>()
				assertEquals(0.5f, atEight.schedule.grid.days.first().nowBlockOffset)

				// The first RESUMED is the sheet opening (ResumedEffect fires for it): nothing restarts.
				fixture.viewModel.resumedAction()
				fixture.viewModel.selectScheduleViewAction(ScheduleViewMode.Week)
				awaitUntilState<Schedule.State.Content> { it.viewMode == ScheduleViewMode.Week }
				assertEquals(1, clock.observations, "the opening does not read the clock again")

				// The device was away: it is 9:30 now and the ticker has not spoken.
				clock.reading = scheduleNow(dayOfWeek = 2, minuteOfDay = 9 * 60 + 30)
				fixture.viewModel.resumedAction()

				val afterClass = awaitUntilState<Schedule.State.Content> { content ->
					content.schedule.grid.days.first().nowBlockOffset == null
				}

				assertFalse(afterClass.schedule.grid.days.first().cells.single().isInProgress)
				assertEquals(2, clock.observations, "one observation at the start and one at the return")

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	// The first resumption is the opening of the sheet: the observation that is already running (or about
	// to start) read the clock, so it restarts nothing.
	@Test
	fun resume_theFirstTime_isTheOpening_andStartsNoSecondObservation() = runTest {
		val clock = SleepingScheduleClockRepository()
		val fixture = createFixture(record = scheduledRecord(), hasSynced = true, clockRepository = clock)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				awaitUntilState<Schedule.State.Content>()

				fixture.viewModel.resumedAction()
				// Actions are processed in order: once this one is seen, the resumption before it was too.
				fixture.viewModel.selectScheduleViewAction(ScheduleViewMode.Week)
				awaitUntilState<Schedule.State.Content> { it.viewMode == ScheduleViewMode.Week }

				cancelAndIgnoreRemainingEvents()
			}
			assertEquals(1, clock.observations)
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun observe_keepsLoadingUntilSync_thenReducesToEmptyWhenNothingIsScheduled() = runTest {
		val fixture = createFixture(record = AcademicRecord(id = "record"), hasSynced = false)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				awaitUntilState<Schedule.State.Loading>()

				fixture.academicRecordRepository.hasSyncedFlow.value = true

				awaitUntilState<Schedule.State.Empty>()

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	// CI5311 meets Monday (blocks 1-2) in the current term of 2026's SEP_DEC period.
	private fun mondayRecord() = AcademicRecord(
		id = "record",
		terms = listOf(
			academicTerm(
				id = "now",
				kind = TermKind.CURRENT,
				periodYear = 2026,
				periodCode = AcademicTermPeriod.SEP_DEC,
				attempts = listOf(
					academicAttempt(subjectCode = "CI5311").copy(
						section = 1,
						schedule = listOf(
							AcademicScheduleEntry(dayOfWeek = 2, startBlock = 1, endBlock = 2, classroom = "MYS-116")
						)
					)
				)
			)
		)
	)

	private fun scheduledRecord() = AcademicRecord(
		id = "record",
		terms = listOf(
			academicTerm(
				id = "now",
				kind = TermKind.CURRENT,
				attempts = listOf(
					academicAttempt(subjectCode = "CI5311").copy(
							section = 1,
							schedule = listOf(
								AcademicScheduleEntry(dayOfWeek = 2, startBlock = 1, endBlock = 2, classroom = "MYS-116")
							)
						)
				)
			)
		)
	)

	private fun createFixture(
		record: AcademicRecord,
		hasSynced: Boolean,
		clockRepository: ScheduleClockRepository = ControllableScheduleClockRepository(),
		visibleEvaluationsRepository: VisibleEvaluationsRepository = ControllableVisibleEvaluationsRepository()
	): ScheduleFixture {
		val academicRecordRepository = ControllableAcademicRecordRepository(
			initialRecord = record,
			initialHasSynced = hasSynced
		)
		val selectionRepository = RecordingScheduleSelectionRepository()
		val reportingRepository = RecordingReportingRepository()

		return ScheduleFixture(
			viewModel = ScheduleViewModel(
				screenMachine = ScheduleMachine(
					observeScheduleUseCase = ObserveScheduleUseCase(
						academicRecordRepository = academicRecordRepository,
						scheduleSelectionRepository = selectionRepository,
						scheduleClockRepository = clockRepository,
						visibleEvaluationsRepository = visibleEvaluationsRepository,
						reportingRepository = reportingRepository
					),
					setScheduleViewModeUseCase = SetScheduleViewModeUseCase(
						scheduleSelectionRepository = selectionRepository,
						reportingRepository = reportingRepository
					)
				),
				eventPublisher = NoOpEventPublisher
			),
			academicRecordRepository = academicRecordRepository,
			selectionRepository = selectionRepository,
			clockRepository = clockRepository
		)
	}

	private class ScheduleFixture(
		val viewModel: ScheduleViewModel,
		val academicRecordRepository: ControllableAcademicRecordRepository,
		val selectionRepository: RecordingScheduleSelectionRepository,
		val clockRepository: ScheduleClockRepository
	)
}
