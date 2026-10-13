package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.testing.MondayAtNine
import com.gdavidpb.tuindice.record.testing.ScheduleTestDate
import com.gdavidpb.tuindice.record.testing.clashingWeek
import com.gdavidpb.tuindice.record.testing.clashingWeekMarks
import com.gdavidpb.tuindice.record.testing.scheduleEvaluation
import kotlinx.datetime.LocalDate
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_day_monday
import tuindice.record.generated.resources.schedule_day_wednesday
import tuindice.record.generated.resources.schedule_table_description_classroom
import tuindice.record.generated.resources.schedule_table_description_meeting
import tuindice.record.generated.resources.schedule_table_description_section
import tuindice.record.generated.resources.schedule_table_meeting_block
import tuindice.record.generated.resources.schedule_table_meeting_blocks
import tuindice.record.generated.resources.schedule_table_meeting_evaluation
import tuindice.record.generated.resources.schedule_table_meeting_in_progress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Where the evaluations of the week land in the schedule. See clashingWeek for the week drawn and
// ScheduleEvaluationMarksMappingTest for which evaluations mark which meeting.
class ScheduleItemEvaluationMarksTest {
	private val wednesday = LocalDate(2026, 10, 7)

	@Test
	fun withoutMarks_noCellCarriesOne_andTheTableExplainsNothing() {
		val item = clashingWeek()

		assertFalse(item.table.hasEvaluationMarks)
		assertTrue(item.table.rows.flatMap { row -> row.meetings.values.flatten() }.none { cell -> cell.hasEvaluation })
	}

	@Test
	fun theMarkedMeetingCarriesTheWeekItAsksFor_andNoOtherDoes() {
		val item = clashingWeek(
			evaluationMarks = clashingWeekMarks(scheduleEvaluation(attemptId = "a1", date = wednesday))
		)

		val a1 = item.table.rows.first { row -> row.attemptId == "a1" }
		val marked = a1.meetings.getValue(ScheduleDay.Wednesday).single()

		assertEquals(ScheduleTestDate, marked.evaluationWeekStart)
		assertTrue(marked.hasEvaluation)
		assertFalse(a1.meetings.getValue(ScheduleDay.Monday).single().hasEvaluation)
		assertTrue(item.table.hasEvaluationMarks)
		// The other subjects stay as they were.
		val others = item.table.rows.filter { row -> row.attemptId != "a1" }

		assertTrue(others.all { row -> row.meetings.values.flatten().none { cell -> cell.hasEvaluation } })
	}

	@Test
	fun theMarkSitsOnTheMeetingOfItsOwnSubject_whenTwoSubjectsMeetAtTheSameTime() {
		// a1 and a2 clash on Monday; the evaluation is a2's.
		val item = clashingWeek(
			evaluationMarks = clashingWeekMarks(scheduleEvaluation(attemptId = "a2", date = ScheduleTestDate))
		)

		val monday = ScheduleDay.Monday

		assertNull(item.table.rows.first { it.attemptId == "a1" }.meetings.getValue(monday).single().evaluationWeekStart)
		assertEquals(
			ScheduleTestDate,
			item.table.rows.first { it.attemptId == "a2" }.meetings.getValue(monday).single().evaluationWeekStart
		)
	}

	@Test
	fun theMarkedMeetingKeepsItsClashAndItsInProgressState() {
		val item = clashingWeek(
			now = MondayAtNine,
			evaluationMarks = clashingWeekMarks(scheduleEvaluation(attemptId = "a1", date = ScheduleTestDate))
		)

		val meeting = item.table.rows.first { it.attemptId == "a1" }.meetings.getValue(ScheduleDay.Monday).single()

		assertTrue(meeting.hasEvaluation)
		assertTrue(meeting.isClash)
		assertTrue(meeting.isInProgress)
	}

	@Test
	fun theRowSaysWhichMeetingHasAnEvaluation_thenWhichIsInProgress() {
		val item = clashingWeek(
			now = MondayAtNine,
			evaluationMarks = clashingWeekMarks(scheduleEvaluation(attemptId = "a1", date = ScheduleTestDate))
		)

		val monday = UiText.Resource(
			Res.string.schedule_table_meeting_blocks,
			listOf(UiText.Resource(Res.string.schedule_day_monday), 1, 2)
		)
		val mondayMeeting = UiText.Resource(
			Res.string.schedule_table_meeting_in_progress,
			listOf(UiText.Resource(Res.string.schedule_table_meeting_evaluation, listOf(monday)))
		)
		val wednesdayMeeting = UiText.Resource(
			Res.string.schedule_table_meeting_block,
			listOf(UiText.Resource(Res.string.schedule_day_wednesday), 3)
		)
		val subject = UiText.Resource(
			Res.string.schedule_table_description_classroom,
			listOf(UiText.Resource(Res.string.schedule_table_description_section, listOf("CI5311", 1)), "MYS-116")
		)

		val withMonday = UiText.Resource(Res.string.schedule_table_description_meeting, listOf(subject, mondayMeeting))

		assertEquals(
			UiText.Resource(Res.string.schedule_table_description_meeting, listOf(withMonday, wednesdayMeeting)),
			item.table.rows.first { it.attemptId == "a1" }.description
		)
	}

	@Test
	fun theGrid_isDrawnAsWithoutMarks() {
		val marked = clashingWeek(
			evaluationMarks = clashingWeekMarks(scheduleEvaluation(attemptId = "a1", date = wednesday))
		)

		assertEquals(
			clashingWeek().grid.days.map { day -> day.cells.map { it.description } },
			marked.grid.days.map { day -> day.cells.map { it.description } }
		)
	}
}
