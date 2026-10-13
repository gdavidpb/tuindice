package com.gdavidpb.tuindice.evaluations.presentation.contract

import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationDateCalendarItem
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
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
	fun dateCalendar_carriesTheMeetingsAndTheCodeOfTheChosenSubjectAndTheSelectableRange() {
		val range = LocalDate(2026, 9, 1)..LocalDate(2026, 12, 31)
		val state = Evaluation.State.Content(
			selectedAttempt = DEFAULT_EVALUATION_SUBJECT.copy(meetings = listOf(meeting)),
			selectableDateRange = range
		)

		assertEquals(
			EvaluationDateCalendarItem(
				selectableRange = range,
				subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
				meetings = listOf(meeting)
			),
			state.dateCalendar
		)
	}

	@Test
	fun dateCalendar_withoutAChosenSubject_hasNoCodeAndNoMeetings() {
		val calendar = Evaluation.State.Content().dateCalendar

		assertNull(calendar.subjectCode)
		assertTrue(calendar.meetings.isEmpty())
		assertNull(calendar.selectableRange)
	}
}
