package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.AnnotatedString
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDayItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleGridItem
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.presentation.model.TermItemKind
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class TermItemViewUiTest {
	@Test
	fun when_termHasASchedule_then_theSwitchOffersNotesAndSchedule() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermItemView(
				item = termItem(schedule = grid()),
				onAttemptSelectionChange = { _, _, _, _ -> }
			)
		}

		assertNodeVisible(RecordUiTags.TermViewSwitch)
		assertNodeVisible(RecordUiTags.AttemptsList)
		assertNodeHidden(RecordUiTags.ScheduleContainer)
	}

	@Test
	fun when_scheduleIsSelected_then_theGridReplacesTheListAndTheSummaryStays() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermItemView(
				item = termItem(schedule = grid()),
				onAttemptSelectionChange = { _, _, _, _ -> }
			)
		}

		onNodeWithTag(RecordUiTags.TermViewScheduleTab).performClick()

		assertNodeVisible(RecordUiTags.ScheduleContainer)
		assertNodeVisible(RecordUiTags.SelectedTermSummary)
		assertNodeHidden(RecordUiTags.AttemptsList)
		onNodeWithText("Bloques de la universidad, no horas del reloj.").assertIsDisplayed()
		onNodeWithText("Por convenir: EP1420").assertIsDisplayed()

		onNodeWithTag(RecordUiTags.TermViewGradesTab).performClick()

		assertNodeVisible(RecordUiTags.AttemptsList)
	}

	@Test
	fun when_scheduleCellIsShown_then_itSpeaksSubjectDayBlocksAndClassroom() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermItemView(
				item = termItem(schedule = grid()),
				onAttemptSelectionChange = { _, _, _, _ -> }
			)
		}

		onNodeWithTag(RecordUiTags.TermViewScheduleTab).performClick()

		onNodeWithTag(RecordUiTags.scheduleCell(attemptId = "attempt-1", dayCode = 2, startBlock = 1))
			.assertIsDisplayed()
		onNode(hasContentDescription("MA2115, lunes, bloques 1 a 2, aula MYS-116")).assertIsDisplayed()
	}

	@Test
	fun when_termHasNoSchedule_then_thereIsNoSwitch() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermItemView(
				item = termItem(schedule = null),
				onAttemptSelectionChange = { _, _, _, _ -> }
			)
		}

		assertNodeHidden(RecordUiTags.TermViewSwitch)
		assertNodeVisible(RecordUiTags.AttemptsList)
	}

	private fun grid() = ScheduleGridItem(
		blockCount = 3,
		days = listOf(
			ScheduleDayItem(
				day = ScheduleDay.Monday,
				cells = listOf(
					ScheduleCellItem(
						attemptId = "attempt-1",
						codeText = "MA2115",
						classroomText = "MYS-116",
						startBlock = 1,
						endBlock = 2,
						lane = 0,
						laneCount = 1,
						hasError = false
					)
				)
			),
			ScheduleDayItem(day = ScheduleDay.Tuesday, cells = emptyList())
		),
		unscheduledCodes = listOf("EP1420")
	)

	private fun termItem(schedule: ScheduleGridItem?) = TermItem(
		termId = "current-term",
		periodYear = 2026,
		termOrder = 1,
		shortNameText = "SEP-DIC 2026",
		kind = TermItemKind.CURRENT,
		gradeText = AnnotatedString("Δx 4.0000"),
		gradeDelta = null,
		gradeSumText = AnnotatedString("∑x 4.0000"),
		gradeSumDelta = null,
		creditsText = AnnotatedString("⦿ 4"),
		creditsDelta = null,
		isCurrent = true,
		canDelete = false,
		canEdit = false,
		attempts = listOf(
			AttemptItem(
				attemptId = "attempt-1",
				subjectCode = "MA2115",
				grade = 1,
				codeText = "MA2115",
				nameText = "MATEMATICAS 3",
				gradeText = "",
				creditsText = "4 UC",
				codeColor = Color(0xFF1E3A5F),
				codeContainerColor = Color(0xFFDCE8F5),
				isReadOnly = false
			)
		),
		schedule = schedule
	)
}
