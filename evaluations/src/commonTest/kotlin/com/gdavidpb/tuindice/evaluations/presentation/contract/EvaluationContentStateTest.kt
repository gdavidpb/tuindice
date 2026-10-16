package com.gdavidpb.tuindice.evaluations.presentation.contract

import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.evaluations.presentation.mapper.withDateCalendar
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EvaluationContentStateTest {
	private val range = LocalDate(2026, 9, 1)..LocalDate(2026, 12, 31)

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
	fun withDateCalendar_carriesTheClassDaysOfTheChosenSubjectAndTheSelectableRange() {
		val state = Evaluation.State.Content(
			selectedAttempt = DEFAULT_EVALUATION_SUBJECT.copy(meetings = listOf(meeting)),
			selectableDateRange = range
		).withDateCalendar()

		val calendar = state.dateCalendar

		assertEquals(range, calendar.selectableRange)
		assertEquals(DEFAULT_EVALUATION_SUBJECT.code, calendar.subjectCode)
		// The subject meets on Mondays: every Monday from September to December 2026 is a class day, and only those.
		assertEquals(17, calendar.classDates.size)
		assertTrue(calendar.classDates.all { date -> date.dayOfWeek == DayOfWeek.MONDAY && date in range })
		assertEquals(calendar.classDates, calendar.classPlaces.keys)
	}

	@Test
	fun withDateCalendar_followsTheSubjectWhenItChanges() {
		val withClassDays = Evaluation.State.Content(
			selectedAttempt = DEFAULT_EVALUATION_SUBJECT.copy(meetings = listOf(meeting)),
			selectableDateRange = range
		).withDateCalendar()

		val withoutClassDays = withClassDays.copy(
			selectedAttempt = DEFAULT_EVALUATION_SUBJECT.copy(meetings = emptyList())
		).withDateCalendar()

		assertEquals(range, withoutClassDays.dateCalendar.selectableRange)
		assertTrue(withoutClassDays.dateCalendar.classDates.isEmpty())
		assertTrue(withoutClassDays.dateCalendar.classPlaces.isEmpty())
	}

	@Test
	fun dateCalendar_ofAContentWithoutAChosenSubject_hasNoCodeAndNoClassDays() {
		val calendar = Evaluation.State.Content().dateCalendar

		assertNull(calendar.subjectCode)
		assertTrue(calendar.classDates.isEmpty())
		assertTrue(calendar.classPlaces.isEmpty())
		assertNull(calendar.selectableRange)
	}
}
