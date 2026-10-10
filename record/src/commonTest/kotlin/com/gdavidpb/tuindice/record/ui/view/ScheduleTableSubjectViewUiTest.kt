package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.presentation.model.ScheduleTableRowItem
import com.gdavidpb.tuindice.record.testing.clashingWeek
import com.gdavidpb.tuindice.record.testing.scheduleAttempt
import com.gdavidpb.tuindice.record.testing.scheduleEntry
import com.gdavidpb.tuindice.record.testing.scheduleItem
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ScheduleTableSubjectViewUiTest {
	@Test
	fun when_sectionAndClassroomAreKnown_then_bothSitUnderTheCode() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleTableSubjectView(row = clashingWeek().table.rows[0])
		}

		onNodeWithText("CI5311").assertIsDisplayed()
		onNodeWithText("Sec. 1 · MYS-116").assertIsDisplayed()
	}

	@Test
	fun when_onlyTheSectionIsKnown_then_itStandsAloneUnderTheCode() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleTableSubjectView(row = clashingWeek().table.rows[1])
		}

		onNodeWithText("CI5437").assertIsDisplayed()
		onNodeWithText("Sec. 2").assertIsDisplayed()
	}

	@Test
	fun when_theMeetingsNameDifferentRooms_then_onlyTheSectionIsShown() = runTuIndiceUiTest {
		val row = row(
			scheduleAttempt(
				id = "a1",
				code = "FS2211",
				section = 3,
				schedule = listOf(
					scheduleEntry(day = ScheduleDay.Monday, blocks = 1..2, classroom = "MYS-116"),
					scheduleEntry(day = ScheduleDay.Wednesday, blocks = 1..2, classroom = "ENE-001")
				)
			)
		)

		setTuIndiceTestContent {
			ScheduleTableSubjectView(row = row)
		}

		onNodeWithText("Sec. 3").assertIsDisplayed()
		onAllNodesWithText("MYS-116", substring = true).assertCountEquals(0)
		onAllNodesWithText("ENE-001", substring = true).assertCountEquals(0)
	}

	@Test
	fun when_onlyTheClassroomIsKnown_then_itStandsAloneUnderTheCode() = runTuIndiceUiTest {
		val row = row(
			scheduleAttempt(
				id = "a1",
				code = "FS2211",
				schedule = listOf(scheduleEntry(day = ScheduleDay.Monday, blocks = 1..2, classroom = "MYS-116"))
			)
		)

		setTuIndiceTestContent {
			ScheduleTableSubjectView(row = row)
		}

		onNodeWithText("FS2211").assertIsDisplayed()
		onNodeWithText("MYS-116").assertIsDisplayed()
		onAllNodesWithText("Sec.", substring = true).assertCountEquals(0)
	}

	@Test
	fun when_neitherIsKnown_then_theCodeIsAllTheSubjectShows() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleTableSubjectView(row = clashingWeek().table.rows[2])
		}

		onNodeWithText("EG1114").assertIsDisplayed()
		// The code is the only text drawn: no empty detail line under it.
		onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text)).assertCountEquals(1)
	}

	private fun row(attempt: AttemptProjection): ScheduleTableRowItem = scheduleItem(attempt).table.rows.single()
}
