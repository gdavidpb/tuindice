package com.gdavidpb.tuindice.evaluations.presentation.contract

import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EvaluationContentStateTest {
	private val meeting = ScheduleMeeting(
		attemptId = DEFAULT_EVALUATION_SUBJECT.id,
		subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
		section = 1,
		dayOfWeek = 2,
		startBlock = 1,
		endBlock = 2,
		classroom = "MYS-116"
	)

	@Test
	fun dateCalendar_carriesTheClassDaysAndTheCodeOfTheChosenSubjectAndTheSelectableRange() {
		val range = LocalDate(2026, 9, 1)..LocalDate(2026, 12, 31)
		val state = Evaluation.State.Content(
			selectedAttempt = DEFAULT_EVALUATION_SUBJECT.copy(meetings = listOf(meeting)),
			selectableDateRange = range
		)

		val calendar = state.dateCalendar

		assertEquals(range, calendar.selectableRange)
		assertEquals(DEFAULT_EVALUATION_SUBJECT.code, calendar.subjectCode)
		// The subject meets on Mondays: every Monday from September to December 2026 is a class day, and only those.
		assertEquals(17, calendar.classDates.size)
		assertTrue(calendar.classDates.all { date -> date.dayOfWeek == DayOfWeek.MONDAY && date in range })
		assertEquals(calendar.classDates, calendar.classPlaces.keys)
	}

	@Test
	fun dateCalendar_withoutAChosenSubject_hasNoCodeAndNoClassDays() {
		val calendar = Evaluation.State.Content().dateCalendar

		assertNull(calendar.subjectCode)
		assertTrue(calendar.classDates.isEmpty())
		assertTrue(calendar.classPlaces.isEmpty())
		assertNull(calendar.selectableRange)
	}
}
