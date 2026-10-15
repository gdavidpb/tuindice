package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMoment
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AcademicScheduleEngineTest {
	private companion object {
		const val SUNDAY = 1
		const val MONDAY = 2
		const val WEDNESDAY = 4
		const val SATURDAY = 7
	}

	private fun meeting(
		code: String,
		day: Int,
		start: Int,
		end: Int = start,
		attemptId: String = "attempt-$code"
	) = ScheduleMeeting(
		attemptId = attemptId,
		subjectCode = code,
		section = 1,
		dayOfWeek = day,
		startBlock = start,
		endBlock = end,
		classroom = "R1"
	)

	private fun minuteOf(block: Int, offset: Int = 0) = ScheduleBlockClock.blockStartMinute(block) + offset

	@Test
	fun isPlaceable_acceptsADayOfTheWeekAndASaneBlockRange() {
		assertTrue(AcademicScheduleEngine.isPlaceable(AcademicScheduleEntry(MONDAY, 1, 1)))
		assertTrue(AcademicScheduleEngine.isPlaceable(AcademicScheduleEntry(SUNDAY, 3, 4)))
		assertTrue(AcademicScheduleEngine.isPlaceable(AcademicScheduleEntry(SATURDAY, 23, 24)))
	}

	@Test
	fun isPlaceable_rejectsMalformedEntries() {
		assertFalse(AcademicScheduleEngine.isPlaceable(AcademicScheduleEntry(0, 1, 2)))
		assertFalse(AcademicScheduleEngine.isPlaceable(AcademicScheduleEntry(8, 1, 2)))
		assertFalse(AcademicScheduleEngine.isPlaceable(AcademicScheduleEntry(MONDAY, 0, 2)))
		assertFalse(AcademicScheduleEngine.isPlaceable(AcademicScheduleEntry(MONDAY, 3, 2)))
		assertFalse(AcademicScheduleEngine.isPlaceable(AcademicScheduleEntry(MONDAY, 24, 25)))
	}

	@Test
	fun meetingsOn_andCurrentOrNext_requireADayBetweenOneAndSeven() {
		listOf(0, 8, -1).forEach { day ->
			assertFailsWith<IllegalArgumentException> { AcademicScheduleEngine.meetingsOn(emptyList(), day) }
			assertFailsWith<IllegalArgumentException> { AcademicScheduleEngine.currentOrNext(emptyList(), day, 0) }
		}
	}

	@Test
	fun currentOrNext_whenOneClassEndsWhereTheNextStarts_theNextOneTakesOverAtItsFirstMinute() {
		val meetings = listOf(meeting("AA", MONDAY, 1, 2), meeting("BB", MONDAY, 3, 4))

		assertEquals(
			ScheduleMoment.InProgress(meetings[0]),
			AcademicScheduleEngine.currentOrNext(meetings, MONDAY, ScheduleBlockClock.blockStartMinute(3) - 1)
		)
		assertEquals(
			ScheduleMoment.InProgress(meetings[1]),
			AcademicScheduleEngine.currentOrNext(meetings, MONDAY, ScheduleBlockClock.blockStartMinute(3))
		)
	}

	@Test
	fun currentOrNext_aMinutePastTheLastBlockOfTheDay_leavesNothingInProgress() {
		val meetings = listOf(meeting("AA", MONDAY, 1, 2), meeting("BB", MONDAY, 3, 4), meeting("CC", WEDNESDAY, 2))

		assertEquals(
			ScheduleMoment.Next(meetings[2], daysAhead = 2),
			AcademicScheduleEngine.currentOrNext(meetings, MONDAY, ScheduleBlockClock.blockEndMinute(4))
		)
		assertEquals(
			ScheduleMoment.Next(meetings[2], daysAhead = 2),
			AcademicScheduleEngine.currentOrNext(meetings, MONDAY, ScheduleBlockClock.MINUTES_PER_DAY - 1)
		)
	}

	@Test
	fun meetingsOn_filtersTheDayAndSortsByStartBlockThenCode() {
		val meetings = listOf(
			meeting("ZZ", MONDAY, 3),
			meeting("BB", MONDAY, 1),
			meeting("AA", MONDAY, 3),
			meeting("CC", WEDNESDAY, 1)
		)

		assertEquals(listOf("BB", "AA", "ZZ"), AcademicScheduleEngine.meetingsOn(meetings, MONDAY).map { it.subjectCode })
		assertTrue(AcademicScheduleEngine.meetingsOn(meetings, SATURDAY).isEmpty())
	}

	@Test
	fun currentOrNext_isNullWithoutMeetings() {
		assertNull(AcademicScheduleEngine.currentOrNext(emptyList(), MONDAY, minuteOf(1)))
	}

	@Test
	fun currentOrNext_inTheFirstMinuteOfABlock_isInProgress() {
		val meetings = listOf(meeting("AA", MONDAY, 2, 3))

		assertEquals(
			ScheduleMoment.InProgress(meetings.single()),
			AcademicScheduleEngine.currentOrNext(meetings, MONDAY, minuteOf(2))
		)
	}

	@Test
	fun currentOrNext_inTheLastMinuteOfTheEndBlock_isStillInProgress() {
		val meetings = listOf(meeting("AA", MONDAY, 2, 3))

		assertEquals(
			ScheduleMoment.InProgress(meetings.single()),
			AcademicScheduleEngine.currentOrNext(meetings, MONDAY, ScheduleBlockClock.blockEndMinute(3) - 1)
		)
	}

	@Test
	fun currentOrNext_theMinuteAfterTheEndBlock_isNoLongerInProgress() {
		val meetings = listOf(meeting("AA", MONDAY, 2, 3))

		val moment = AcademicScheduleEngine.currentOrNext(meetings, MONDAY, ScheduleBlockClock.blockEndMinute(3))

		// Nothing is left today and the only meeting is on today's weekday: it is next week's, seven days ahead.
		assertEquals(ScheduleMoment.Next(meetings.single(), daysAhead = 7), moment)
	}

	@Test
	fun currentOrNext_beforeTheFirstBlock_nextIsTheFirstMeetingOfToday() {
		val meetings = listOf(meeting("BB", MONDAY, 3), meeting("AA", MONDAY, 1))

		assertEquals(
			ScheduleMoment.Next(meetings[1], daysAhead = 0),
			AcademicScheduleEngine.currentOrNext(meetings, MONDAY, ScheduleBlockClock.FIRST_BLOCK_START_MINUTE - 1)
		)
		assertEquals(
			ScheduleMoment.Next(meetings[1], daysAhead = 0),
			AcademicScheduleEngine.currentOrNext(meetings, MONDAY, 0)
		)
	}

	@Test
	fun currentOrNext_betweenTwoMeetings_nextIsTheLaterOneOfToday() {
		val meetings = listOf(meeting("AA", MONDAY, 1), meeting("BB", MONDAY, 4))

		assertEquals(
			ScheduleMoment.Next(meetings[1], daysAhead = 0),
			AcademicScheduleEngine.currentOrNext(meetings, MONDAY, minuteOf(2, offset = 10))
		)
	}

	@Test
	fun currentOrNext_withTwoMeetingsAtOnce_theFirstByStartBlockAndCodeIsInProgress() {
		val meetings = listOf(meeting("ZZ", MONDAY, 2, 3), meeting("AA", MONDAY, 2, 2), meeting("MM", MONDAY, 1, 4))

		assertEquals(
			ScheduleMoment.InProgress(meetings[2]),
			AcademicScheduleEngine.currentOrNext(meetings, MONDAY, minuteOf(2))
		)
		assertEquals(
			ScheduleMoment.InProgress(meetings[1]),
			AcademicScheduleEngine.currentOrNext(listOf(meetings[0], meetings[1]), MONDAY, minuteOf(2))
		)
	}

	@Test
	fun currentOrNext_afterTheLastBlockOfToday_nextIsTheFirstOfTheNextDayWithClasses() {
		val meetings = listOf(meeting("AA", MONDAY, 1), meeting("BB", WEDNESDAY, 5), meeting("CC", WEDNESDAY, 2))

		assertEquals(
			ScheduleMoment.Next(meetings[2], daysAhead = 2),
			AcademicScheduleEngine.currentOrNext(meetings, MONDAY, minuteOf(10))
		)
	}

	@Test
	fun currentOrNext_wrapsFromSaturdayToSunday() {
		val meetings = listOf(meeting("AA", SUNDAY, 2))

		assertEquals(
			ScheduleMoment.Next(meetings.single(), daysAhead = 1),
			AcademicScheduleEngine.currentOrNext(meetings, SATURDAY, minuteOf(5))
		)
	}

	@Test
	fun currentOrNext_looksAheadSevenDaysAtMost() {
		val sixAhead = listOf(meeting("AA", SUNDAY, 1))

		// Monday + 6 days = Sunday.
		assertEquals(
			ScheduleMoment.Next(sixAhead.single(), daysAhead = 6),
			AcademicScheduleEngine.currentOrNext(sixAhead, MONDAY, minuteOf(5))
		)
		// A meeting only on today's weekday that has already passed is the same weekday next week.
		val sameDayNextWeek = listOf(meeting("BB", MONDAY, 1))

		assertEquals(
			ScheduleMoment.Next(sameDayNextWeek.single(), daysAhead = 7),
			AcademicScheduleEngine.currentOrNext(sameDayNextWeek, MONDAY, minuteOf(5))
		)
		assertNull(AcademicScheduleEngine.currentOrNext(emptyList(), MONDAY, minuteOf(5)))
	}

	@Test
	fun dayOfWeekOf_mapsDatesToTheModelsDays() {
		// 2026-10-11 is a Sunday.
		assertEquals(SUNDAY, AcademicScheduleEngine.dayOfWeekOf(LocalDate(2026, 10, 11)))
		assertEquals(MONDAY, AcademicScheduleEngine.dayOfWeekOf(LocalDate(2026, 10, 12)))
		assertEquals(WEDNESDAY, AcademicScheduleEngine.dayOfWeekOf(LocalDate(2026, 10, 14)))
		assertEquals(SATURDAY, AcademicScheduleEngine.dayOfWeekOf(LocalDate(2026, 10, 17)))
	}

	@Test
	fun meetingFor_isTheFirstMeetingOfTheAttemptOnThatWeekday() {
		val meetings = listOf(
			meeting("AA", MONDAY, 5),
			meeting("AA", MONDAY, 2),
			meeting("AA", WEDNESDAY, 1)
		)

		assertEquals(meetings[1], AcademicScheduleEngine.meetingFor(meetings, LocalDate(2026, 10, 12)))
		assertEquals(meetings[2], AcademicScheduleEngine.meetingFor(meetings, LocalDate(2026, 10, 14)))
		assertNull(AcademicScheduleEngine.meetingFor(meetings, LocalDate(2026, 10, 13)))
		assertNull(AcademicScheduleEngine.meetingFor(emptyList(), LocalDate(2026, 10, 12)))
	}

	@Test
	fun inheritedMeeting_ofADatedEvaluationIsTheFirstMeetingThatWeekday() {
		val meetings = listOf(
			meeting("AA", MONDAY, 5, 6),
			meeting("AA", MONDAY, 2, 3),
			meeting("AA", WEDNESDAY, 1)
		)

		// Monday 2026-10-12: two meetings, the earlier block wins whatever the list order is.
		assertEquals(
			meetings[1],
			AcademicScheduleEngine.inheritedMeeting(meetings, EvaluationScheduleMode.DATED, LocalDate(2026, 10, 12))
		)
		assertEquals(
			meetings[2],
			AcademicScheduleEngine.inheritedMeeting(meetings, EvaluationScheduleMode.DATED, LocalDate(2026, 10, 14))
		)
	}

	@Test
	fun inheritedMeeting_isNullOnADayWithoutClass() {
		val meetings = listOf(meeting("AA", MONDAY, 2, 3))

		assertNull(
			AcademicScheduleEngine.inheritedMeeting(meetings, EvaluationScheduleMode.DATED, LocalDate(2026, 10, 13))
		)
	}

	@Test
	fun inheritedMeeting_isNullForAContinuousEvaluationEvenWithADate() {
		val meetings = listOf(meeting("AA", MONDAY, 2, 3))

		assertNull(
			AcademicScheduleEngine.inheritedMeeting(meetings, EvaluationScheduleMode.CONTINUOUS, LocalDate(2026, 10, 12))
		)
		assertNull(AcademicScheduleEngine.inheritedMeeting(meetings, EvaluationScheduleMode.CONTINUOUS, null))
	}

	@Test
	fun inheritedMeeting_isNullWithoutADate() {
		val meetings = listOf(meeting("AA", MONDAY, 2, 3))

		assertNull(AcademicScheduleEngine.inheritedMeeting(meetings, EvaluationScheduleMode.DATED, null))
	}

	@Test
	fun inheritedMeeting_isNullWhenTheTermHasNoSchedule() {
		assertNull(
			AcademicScheduleEngine.inheritedMeeting(emptyList(), EvaluationScheduleMode.DATED, LocalDate(2026, 10, 12))
		)
	}

	@Test
	fun inheritedMeeting_followsTheModelsSundayAndSaturday() {
		val meetings = listOf(meeting("AA", SUNDAY, 1), meeting("AA", SATURDAY, 3, 4))

		// 2026-10-11 is a Sunday and 2026-10-17 a Saturday.
		assertEquals(
			meetings[0],
			AcademicScheduleEngine.inheritedMeeting(meetings, EvaluationScheduleMode.DATED, LocalDate(2026, 10, 11))
		)
		assertEquals(
			meetings[1],
			AcademicScheduleEngine.inheritedMeeting(meetings, EvaluationScheduleMode.DATED, LocalDate(2026, 10, 17))
		)
	}
}
