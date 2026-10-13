package com.gdavidpb.tuindice.record.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.domain.model.ScheduleNow
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.record.domain.model.ObservedSchedule
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.testing.ControllableAcademicRecordRepository
import com.gdavidpb.tuindice.record.testing.ControllableScheduleClockRepository
import com.gdavidpb.tuindice.record.testing.ControllableVisibleEvaluationsRepository
import com.gdavidpb.tuindice.record.testing.RecordingScheduleSelectionRepository
import com.gdavidpb.tuindice.record.testing.academicAttempt
import com.gdavidpb.tuindice.record.testing.academicTerm
import com.gdavidpb.tuindice.record.testing.scheduleEvaluation
import com.gdavidpb.tuindice.record.testing.scheduleNow
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ObserveScheduleUseCaseTest {
	@Test
	fun execute_emitsTheCurrentTermWithItsMeetings_andTheStoredView() = runTest {
		val record = AcademicRecord(
			id = "record",
			terms = listOf(
				academicTerm(id = "old", kind = TermKind.HISTORICAL),
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

		val observed = observe(
			record = record,
			hasSynced = true,
			viewMode = ScheduleViewMode.Week
		)

		assertEquals("now", observed.currentTerm?.id)
		assertEquals(listOf("CI5311"), observed.currentTerm?.attempts?.map { it.subjectCode })
		assertEquals(ScheduleViewMode.Week, observed.viewMode)
		assertTrue(observed.hasSyncedRecord)
		assertEquals(scheduleNow(dayOfWeek = 2, minuteOfDay = 8 * 60), observed.now)
	}

	@Test
	fun execute_emitsAgainWhenTheMinuteChanges() = runTest {
		val clockRepository = ControllableScheduleClockRepository()
		val useCase = ObserveScheduleUseCase(
			academicRecordRepository = ControllableAcademicRecordRepository(initialHasSynced = true),
			scheduleSelectionRepository = RecordingScheduleSelectionRepository(),
			scheduleClockRepository = clockRepository,
			visibleEvaluationsRepository = ControllableVisibleEvaluationsRepository(),
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertEquals(8 * 60, awaitLoadingThenData(this).now.minuteOfDay)

			clockRepository.nowFlow.value = scheduleNow(dayOfWeek = 2, minuteOfDay = 8 * 60 + 1)

			val next = assertIs<UseCaseState.Data<ObservedSchedule>>(awaitItem())

			assertEquals(8 * 60 + 1, next.value.now.minuteOfDay)

			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun execute_withTwoCurrentTermsInAnyOrder_emitsTheMostRecentOne() = runTest {
		val older = academicTerm(id = "older", kind = TermKind.CURRENT, periodYear = 2025)
		val newer = academicTerm(id = "newer", kind = TermKind.CURRENT, periodYear = 2026)
		val historical = academicTerm(id = "historical", kind = TermKind.HISTORICAL, periodYear = 2027)

		listOf(
			listOf(older, newer, historical),
			listOf(historical, newer, older),
			listOf(newer, historical, older)
		).forEach { terms ->
			val observed = observe(
				record = AcademicRecord(id = "record", terms = terms),
				hasSynced = true,
				viewMode = ScheduleViewMode.Week
			)

			assertEquals("newer", observed.currentTerm?.id, "terms: ${terms.map { term -> term.id }}")
		}
	}

	@Test
	fun execute_emitsTheWeekOfTheTermTheClockFallsIn() = runTest {
		// Week 1 opens on Monday 2026-08-31 (the week of the period's first day), so Monday 2026-10-05 is week 6.
		val byPeriod = observe(record = wednesdayRecord(), hasSynced = true, viewMode = ScheduleViewMode.Table)

		assertEquals(6, byPeriod.academicWeek)

		// With the real first day of classes the weeks follow it: the Monday 2026-09-14 opens week 1.
		val byClassesStart = observe(
			record = wednesdayRecord(classesStartDate = "2026-09-14"),
			hasSynced = true,
			viewMode = ScheduleViewMode.Table
		)

		assertEquals(4, byClassesStart.academicWeek)
	}

	@Test
	fun execute_outsideTheWeeksOfTheTerm_emitsNoWeek() = runTest {
		// Week 12 ends on Sunday 2026-11-22: the Monday after is already outside, and so is the term's eve.
		listOf(LocalDate(2026, 11, 23), LocalDate(2026, 8, 30)).forEach { date ->
			val observed = observe(
				record = wednesdayRecord(),
				hasSynced = true,
				viewMode = ScheduleViewMode.Table,
				now = ScheduleNow(dayOfWeek = 2, minuteOfDay = 8 * 60, date = date)
			)

			assertNull(observed.academicWeek, "date: $date")
		}

		assertNull(
			observe(
				record = AcademicRecord(id = "record"),
				hasSynced = true,
				viewMode = ScheduleViewMode.Table
			).academicWeek
		)
	}

	@Test
	fun execute_marksTheMeetingsWithAnEvaluationThisWeek() = runTest {
		val evaluations = ControllableVisibleEvaluationsRepository(
			initialEvaluations = listOf(
				scheduleEvaluation(attemptId = "attempt-CI5311", date = LocalDate(2026, 10, 7)),
				// Next week's Wednesday: not this week.
				scheduleEvaluation(attemptId = "attempt-CI5311", date = LocalDate(2026, 10, 14))
			)
		)

		val observed = observe(
			record = wednesdayRecord(),
			hasSynced = true,
			viewMode = ScheduleViewMode.Table,
			evaluations = evaluations
		)

		assertEquals(LocalDate(2026, 10, 5), observed.evaluationMarks.weekStart)
		assertEquals(listOf(4), observed.evaluationMarks.meetings.map { it.dayOfWeek })
	}

	@Test
	fun execute_withEvaluationsNotDownloadedYet_emitsTheScheduleWithoutMarks() = runTest {
		val evaluations = ControllableVisibleEvaluationsRepository(initialHasSynced = false)

		val observed = observe(
			record = wednesdayRecord(),
			hasSynced = true,
			viewMode = ScheduleViewMode.Table,
			evaluations = evaluations
		)

		assertEquals("now", observed.currentTerm?.id)
		assertTrue(observed.evaluationMarks.meetings.isEmpty())
	}

	@Test
	fun execute_whenReadingTheEvaluationsFails_emitsTheScheduleWithoutMarks_andReportsIt() = runTest {
		val reporting = RecordingReportingRepository()
		val failure = IllegalStateException("local database is gone")
		val evaluations = ControllableVisibleEvaluationsRepository(failure = failure)

		val useCase = ObserveScheduleUseCase(
			academicRecordRepository = ControllableAcademicRecordRepository(
				initialRecord = wednesdayRecord(),
				initialHasSynced = true
			),
			scheduleSelectionRepository = RecordingScheduleSelectionRepository(),
			scheduleClockRepository = ControllableScheduleClockRepository(),
			visibleEvaluationsRepository = evaluations,
			reportingRepository = reporting
		)

		useCase.execute(Unit).test {
			val observed = awaitLoadingThenData(this)

			assertEquals("now", observed.currentTerm?.id)
			assertTrue(observed.evaluationMarks.meetings.isEmpty())
			assertEquals(listOf<Throwable>(failure), reporting.loggedExceptions)

			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun execute_emitsAgainWhenTheEvaluationsChange() = runTest {
		val evaluations = ControllableVisibleEvaluationsRepository()
		val useCase = ObserveScheduleUseCase(
			academicRecordRepository = ControllableAcademicRecordRepository(
				initialRecord = wednesdayRecord(),
				initialHasSynced = true
			),
			scheduleSelectionRepository = RecordingScheduleSelectionRepository(),
			scheduleClockRepository = ControllableScheduleClockRepository(),
			visibleEvaluationsRepository = evaluations,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertTrue(awaitLoadingThenData(this).evaluationMarks.meetings.isEmpty())

			evaluations.snapshotFlow.value = ObservedSyncedSnapshot(
				value = listOf(scheduleEvaluation(attemptId = "attempt-CI5311", date = LocalDate(2026, 10, 7))),
				hasSynced = true
			)

			val next = assertIs<UseCaseState.Data<ObservedSchedule>>(awaitItem())

			assertEquals(1, next.value.evaluationMarks.meetings.size)

			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun execute_withoutACurrentTerm_emitsNoTerm() = runTest {
		val observed = observe(
			record = AcademicRecord(id = "record", terms = listOf(academicTerm(id = "old"))),
			hasSynced = false,
			viewMode = ScheduleViewMode.Table
		)

		assertNull(observed.currentTerm)
		assertFalse(observed.hasSyncedRecord)
	}

	// A current term of 2026's SEP_DEC period with CI5311 meeting Wednesday (block 3).
	private fun wednesdayRecord(classesStartDate: String? = null) = AcademicRecord(
		id = "record",
		terms = listOf(
			academicTerm(
				id = "now",
				kind = TermKind.CURRENT,
				periodYear = 2026,
				periodCode = AcademicTermPeriod.SEP_DEC,
				attempts = listOf(
					academicAttempt(subjectCode = "CI5311").copy(
						schedule = listOf(
							AcademicScheduleEntry(dayOfWeek = 4, startBlock = 3, endBlock = 3, classroom = "MYS-116")
						)
					)
				)
			).copy(classesStartDate = classesStartDate)
		)
	)

	private suspend fun observe(
		record: AcademicRecord,
		hasSynced: Boolean,
		viewMode: ScheduleViewMode,
		now: ScheduleNow = scheduleNow(dayOfWeek = 2, minuteOfDay = 8 * 60),
		evaluations: ControllableVisibleEvaluationsRepository = ControllableVisibleEvaluationsRepository()
	): ObservedSchedule {
		val useCase = ObserveScheduleUseCase(
			academicRecordRepository = ControllableAcademicRecordRepository(
				initialRecord = record,
				initialHasSynced = hasSynced
			),
			scheduleSelectionRepository = RecordingScheduleSelectionRepository(initialViewMode = viewMode),
			scheduleClockRepository = ControllableScheduleClockRepository(initialNow = now),
			visibleEvaluationsRepository = evaluations,
			reportingRepository = RecordingReportingRepository()
		)
		var observed: ObservedSchedule? = null

		useCase.execute(Unit).test {
			observed = awaitLoadingThenData(this)

			cancelAndIgnoreRemainingEvents()
		}

		return requireNotNull(observed)
	}
}
