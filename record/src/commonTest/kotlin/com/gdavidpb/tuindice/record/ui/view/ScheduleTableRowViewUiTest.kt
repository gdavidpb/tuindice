package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.record.presentation.mapper.toScheduleItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.presentation.model.ScheduleTableItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
class ScheduleTableRowViewUiTest {
	@Test
	fun when_theSubjectOverlapsAnother_then_theRowMarksTheMeetingAndSaysTheClashUnderIt() = runTuIndiceUiTest {
		val table = table()

		setTuIndiceTestContent {
			ScheduleTableRowView(row = table.rows.first(), days = table.days)
		}

		onNodeWithText("Sec. 1 · MYS-116").assertIsDisplayed()
		onNodeWithText("Choque de horario").assertIsDisplayed()
		assertNodeVisible(RecordUiTags.scheduleTableError("a1"))
		// The clash mark lives inside the merged row, so it is read from the unmerged tree.
		assertNodeVisible(RecordUiTags.scheduleTableClash("a1", ScheduleDay.Monday.code), useUnmergedTree = true)
		assertNodeHidden(RecordUiTags.scheduleTableClash("a1", ScheduleDay.Wednesday.code), useUnmergedTree = true)
		onNode(hasContentDescription("CI5311, sección 1, aula MYS-116, lunes bloques 1 a 2, miércoles bloque 3"))
			.assertIsDisplayed()
	}

	@Test
	fun when_theSubjectHasNoMeeting_then_theRowSaysSinHorarioAndNoClash() = runTuIndiceUiTest {
		val table = table()

		setTuIndiceTestContent {
			ScheduleTableRowView(row = table.rows.last(), days = table.days)
		}

		onNodeWithText("Sin horario").assertIsDisplayed()
		assertNodeVisible(RecordUiTags.scheduleTableUnscheduled("a3"), useUnmergedTree = true)
		assertNodeHidden(RecordUiTags.scheduleTableError("a3"))
		onAllNodesWithText("Choque de horario").assertCountEquals(0)
		onNode(hasContentDescription("EG1114, sin horario")).assertIsDisplayed()
	}

	// Laid out by the mapper, so the row draws what the app resolves.
	private fun table(): ScheduleTableItem = assertNotNull(
		listOf(
			attempt(
				id = "a1",
				code = "CI5311",
				section = 1,
				schedule = listOf(
					entry(day = ScheduleDay.Monday, blocks = 1..2),
					entry(day = ScheduleDay.Wednesday, blocks = 3..3)
				)
			),
			attempt(id = "a2", code = "CI5437", schedule = listOf(entry(day = ScheduleDay.Monday, blocks = 2..3))),
			attempt(id = "a3", code = "EG1114")
		).toScheduleItem()
	).table

	private fun entry(day: ScheduleDay, blocks: IntRange) = AcademicScheduleEntry(
		dayOfWeek = day.code,
		startBlock = blocks.first,
		endBlock = blocks.last,
		classroom = "MYS-116"
	)

	private fun attempt(
		id: String,
		code: String,
		section: Int? = null,
		schedule: List<AcademicScheduleEntry>? = null
	) = AttemptProjection(
		id = id,
		subjectCode = code,
		subjectName = code,
		credits = 3,
		gradingMode = AttemptGradingMode.NUMERIC,
		score = AttemptScore.empty(),
		outcome = AttemptOutcome.PENDING,
		badge = AttemptBadge.NONE,
		section = section,
		schedule = schedule
	)
}
