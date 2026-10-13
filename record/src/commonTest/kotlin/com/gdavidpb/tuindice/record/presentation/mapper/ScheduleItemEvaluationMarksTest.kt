package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.presentation.model.ScheduleItem
import com.gdavidpb.tuindice.record.testing.MondayAtNine
import com.gdavidpb.tuindice.record.testing.ScheduleTestDate
import com.gdavidpb.tuindice.record.testing.clashingWeek
import com.gdavidpb.tuindice.record.testing.clashingWeekMarks
import com.gdavidpb.tuindice.record.testing.scheduleEvaluation
import kotlinx.datetime.LocalDate
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_cell_description
import tuindice.record.generated.resources.schedule_cell_description_clash
import tuindice.record.generated.resources.schedule_cell_description_classroom
import tuindice.record.generated.resources.schedule_cell_description_in_progress
import tuindice.record.generated.resources.schedule_cell_description_span
import tuindice.record.generated.resources.schedule_clash
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
	fun withoutMarks_noCellOfTheGridCarriesOne_andTheGridExplainsNothing() {
		val item = clashingWeek()

		assertFalse(item.grid.hasEvaluationMarks)
		assertTrue(item.grid.days.flatMap { day -> day.cells }.none { cell -> cell.hasEvaluation })
		assertEquals(wednesdayDescription(), gridCell(item, ScheduleDay.Wednesday, "a1").description)
	}

	@Test
	fun theMarkedCellOfTheGridCarriesTheWeekItAsksFor_andSaysSo_andNoOtherDoes() {
		val item = clashingWeek(
			evaluationMarks = clashingWeekMarks(scheduleEvaluation(attemptId = "a1", date = wednesday))
		)

		val marked = gridCell(item, ScheduleDay.Wednesday, "a1")

		assertEquals(ScheduleTestDate, marked.evaluationWeekStart)
		assertTrue(item.grid.hasEvaluationMarks)
		assertEquals(
			UiText.Resource(Res.string.schedule_table_meeting_evaluation, listOf(wednesdayDescription())),
			marked.description
		)
		// The other cells keep what they were: the clashing pair of Monday has no mark.
		val others = item.grid.days.flatMap { day -> day.cells }.filter { cell -> cell !== marked }

		assertTrue(others.none { cell -> cell.hasEvaluation })
		assertEquals(
			clashingWeek().grid.days.flatMap { day -> day.cells }
				.filter { cell -> cell.startBlock != 3 || cell.attemptId != "a1" }
				.map { cell -> cell.description },
			others.map { cell -> cell.description }
		)
	}

	@Test
	fun theGridAndTheTableMarkTheSameMeetings() {
		val item = clashingWeek(
			evaluationMarks = clashingWeekMarks(
				scheduleEvaluation(attemptId = "a1", date = wednesday),
				scheduleEvaluation(attemptId = "a2", date = ScheduleTestDate)
			)
		)

		val inGrid = item.grid.days.flatMap { dayItem -> dayItem.cells.markedAt(dayItem.day) }
		val inTable = item.table.rows.flatMap { row ->
			row.meetings.flatMap { (day, cells) -> cells.markedAt(day) }
		}

		assertEquals(2, inGrid.size)
		assertEquals(inGrid.toSet(), inTable.toSet())
	}

	@Test
	fun theMarkedCellOfTheGridKeepsItsClashAndItsInProgressState_andSaysAllThree() {
		val item = clashingWeek(
			now = MondayAtNine,
			evaluationMarks = clashingWeekMarks(scheduleEvaluation(attemptId = "a1", date = ScheduleTestDate))
		)

		val marked = gridCell(item, ScheduleDay.Monday, "a1")

		assertTrue(marked.hasEvaluation)
		assertTrue(marked.isClash)
		assertTrue(marked.isInProgress)

		val timing = UiText.Resource(
			Res.string.schedule_cell_description_classroom,
			listOf(
				UiText.Resource(
					Res.string.schedule_cell_description_span,
					listOf("CI5311", UiText.Resource(Res.string.schedule_day_monday), 1, 2)
				),
				"MYS-116"
			)
		)
		val withEvaluation = UiText.Resource(Res.string.schedule_table_meeting_evaluation, listOf(timing))
		val withClash = UiText.Resource(
			Res.string.schedule_cell_description_clash,
			listOf(withEvaluation, UiText.Resource(Res.string.schedule_clash))
		)

		assertEquals(
			UiText.Resource(Res.string.schedule_cell_description_in_progress, listOf(withClash)),
			marked.description
		)
	}

	@Test
	fun theClashingCellWithoutAnEvaluation_staysUnmarked_whenItsNeighbourHasOne() {
		val item = clashingWeek(
			evaluationMarks = clashingWeekMarks(scheduleEvaluation(attemptId = "a2", date = ScheduleTestDate))
		)

		assertFalse(gridCell(item, ScheduleDay.Monday, "a1").hasEvaluation)
		assertTrue(gridCell(item, ScheduleDay.Monday, "a2").hasEvaluation)
	}

	@Test
	fun twoEvaluationsOnTheSameBlockLeaveOneMarkOnTheCell_andTheDescriptionSaysItOnce() {
		val item = clashingWeek(
			evaluationMarks = clashingWeekMarks(
				scheduleEvaluation(attemptId = "a1", date = wednesday, id = "first"),
				scheduleEvaluation(attemptId = "a1", date = wednesday, id = "second")
			)
		)

		val marked = gridCell(item, ScheduleDay.Wednesday, "a1")

		assertEquals(ScheduleTestDate, marked.evaluationWeekStart)
		assertEquals(1, item.grid.days.flatMap { day -> day.cells }.count { cell -> cell.hasEvaluation })
		assertEquals(
			UiText.Resource(Res.string.schedule_table_meeting_evaluation, listOf(wednesdayDescription())),
			marked.description
		)
	}

	// Which meeting (subject, day) asks for which week, for the cells that carry a mark.
	private fun List<ScheduleCellItem>.markedAt(day: ScheduleDay) = filter { cell -> cell.hasEvaluation }
		.map { cell -> Triple(cell.attemptId, day, cell.evaluationWeekStart) }

	private fun gridCell(item: ScheduleItem, day: ScheduleDay, attemptId: String): ScheduleCellItem {
		return item.grid.days.first { dayItem -> dayItem.day == day }.cells.single { cell -> cell.attemptId == attemptId }
	}

	// "CI5311, miércoles, bloque 3, aula MYS-116": what the Wednesday cell of CI5311 says before any mark.
	private fun wednesdayDescription() = UiText.Resource(
		Res.string.schedule_cell_description_classroom,
		listOf(
			UiText.Resource(
				Res.string.schedule_cell_description,
				listOf("CI5311", UiText.Resource(Res.string.schedule_day_wednesday), 3)
			),
			"MYS-116"
		)
	)
}
