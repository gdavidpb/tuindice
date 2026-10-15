package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EvaluationDateCalendarMappingTest {
	private companion object {
		const val MONDAY = 2
		const val WEDNESDAY = 4

		val OCTOBER = LocalDate(2026, 10, 1)..LocalDate(2026, 10, 31)
	}

	private fun meeting(day: Int, start: Int, end: Int, classroom: String = "MYS-116") = ScheduleMeeting(
		attemptId = DEFAULT_EVALUATION_SUBJECT.id,
		subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
		section = 1,
		dayOfWeek = day,
		startBlock = start,
		endBlock = end,
		classroom = classroom
	)

	// October 2026: the 5th, 12th, 19th and 26th are Mondays, the 14th is a Wednesday.
	private fun octoberDays(vararg days: Int): Set<LocalDate> {
		return days.mapTo(HashSet()) { day -> LocalDate(2026, 10, day) }
	}

	@Test
	fun getEvaluationDateCalendarItem_marksTheClassDaysInsideTheRange() {
		val calendar = getEvaluationDateCalendarItem(
			selectableRange = OCTOBER,
			subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
			meetings = listOf(meeting(day = MONDAY, start = 1, end = 2))
		)

		assertEquals(octoberDays(5, 12, 19, 26), calendar.classDates)
	}

	@Test
	fun getEvaluationDateCalendarItem_leavesOutTheClassDaysOutsideTheRange() {
		val calendar = getEvaluationDateCalendarItem(
			selectableRange = LocalDate(2026, 10, 6)..LocalDate(2026, 10, 20),
			subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
			meetings = listOf(meeting(day = MONDAY, start = 1, end = 2))
		)

		assertEquals(octoberDays(12, 19), calendar.classDates)
	}

	@Test
	fun getEvaluationDateCalendarItem_holdsThePlaceOfEachClassDay() {
		val meetings = listOf(
			meeting(day = MONDAY, start = 1, end = 2),
			meeting(day = WEDNESDAY, start = 3, end = 4, classroom = "MYS-200")
		)

		val calendar = getEvaluationDateCalendarItem(
			selectableRange = OCTOBER,
			subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
			meetings = meetings
		)

		val wednesday = LocalDate(2026, 10, 14)
		val tuesday = LocalDate(2026, 10, 13)

		assertEquals(calendar.classDates, calendar.classPlaces.keys)
		assertEquals(meetings.classPlaceOn(wednesday), calendar.classPlaces[wednesday])
		assertNull(calendar.classPlaces[tuesday])
	}

	@Test
	fun getEvaluationDateCalendarItem_agreesWithTheMeetingsOnEveryDayOfTheRange() {
		val meetings = listOf(
			meeting(day = MONDAY, start = 1, end = 2),
			meeting(day = WEDNESDAY, start = 3, end = 4, classroom = "MYS-200")
		)

		val calendar = getEvaluationDateCalendarItem(
			selectableRange = OCTOBER,
			subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
			meetings = meetings
		)

		for (day in 1..31) {
			val date = LocalDate(2026, 10, day)

			assertEquals(meetings.hasClassOn(date), date in calendar.classDates, "class day of $date")
		}
	}

	@Test
	fun getEvaluationDateCalendarItem_withoutARange_worksOutNothingAndKeepsTheSubject() {
		val calendar = getEvaluationDateCalendarItem(
			selectableRange = null,
			subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
			meetings = listOf(meeting(day = MONDAY, start = 1, end = 2))
		)

		assertNull(calendar.selectableRange)
		assertEquals(DEFAULT_EVALUATION_SUBJECT.code, calendar.subjectCode)
		assertTrue(calendar.classDates.isEmpty())
		assertTrue(calendar.classPlaces.isEmpty())
	}

	@Test
	fun getEvaluationDateCalendarItem_ofASubjectWithoutMeetings_hasNoClassDay() {
		val calendar = getEvaluationDateCalendarItem(
			selectableRange = OCTOBER,
			subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
			meetings = emptyList()
		)

		assertEquals(OCTOBER, calendar.selectableRange)
		assertTrue(calendar.classDates.isEmpty())
		assertTrue(calendar.classPlaces.isEmpty())
	}
}
