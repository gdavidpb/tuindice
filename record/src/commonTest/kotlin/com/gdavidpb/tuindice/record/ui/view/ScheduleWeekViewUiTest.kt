package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.presentation.model.ScheduleCellItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDayItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleGridItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ScheduleWeekViewUiTest {
	@Test
	fun when_theWeekIsTallerThanItsSpace_then_theBlocksScrollUnderTheDayNames() = runTuIndiceUiTest {
		val lastCellTag = RecordUiTags.scheduleCell("a2", ScheduleDay.Monday.code, LastBlock - 1)

		setTuIndiceTestContent {
			ScheduleWeekView(
				modifier = Modifier.height(200.dp),
				grid = ScheduleGridItem(
					blockCount = LastBlock,
					days = listOf(
						ScheduleDayItem(
							day = ScheduleDay.Monday,
							cells = listOf(
								cell(attemptId = "a1", code = "CI5311", blocks = 1..2),
								cell(attemptId = "a2", code = "CI3725", blocks = LastBlock - 1..LastBlock)
							)
						)
					),
					unscheduledCodes = listOf("EG1114")
				)
			)
		}

		onNodeWithTag(lastCellTag).assertIsNotDisplayed()
		onNodeWithText("Sin horario: EG1114").assertIsDisplayed()

		repeat(times = 4) {
			onNodeWithTag(RecordUiTags.ScheduleGrid).performTouchInput { swipeUp() }
		}

		onNodeWithTag(lastCellTag).assertIsDisplayed()
		onNodeWithText("Lun").assertIsDisplayed()
		onNodeWithText("Sin horario: EG1114").assertIsDisplayed()
	}

	@Test
	fun when_theWeekIsWiderThanItsSpace_then_theDaysScrollPastTheBlockNumbers() = runTuIndiceUiTest {
		val saturdayCellTag = RecordUiTags.scheduleCell("a2", ScheduleDay.Saturday.code, 1)
		val weekdays = listOf(
			ScheduleDay.Monday,
			ScheduleDay.Tuesday,
			ScheduleDay.Wednesday,
			ScheduleDay.Thursday,
			ScheduleDay.Friday
		)

		setTuIndiceTestContent {
			ScheduleWeekView(
				modifier = Modifier.width(200.dp),
				grid = ScheduleGridItem(
					blockCount = 2,
					days = weekdays.map { day -> ScheduleDayItem(day = day, cells = emptyList()) } + ScheduleDayItem(
						day = ScheduleDay.Saturday,
						cells = listOf(cell(attemptId = "a2", code = "FS2211", blocks = 1..2))
					),
					unscheduledCodes = emptyList()
				)
			)
		}

		onNodeWithTag(saturdayCellTag).assertIsNotDisplayed()

		repeat(times = 4) {
			onNodeWithTag(RecordUiTags.ScheduleGrid).performTouchInput { swipeLeft() }
		}

		onNodeWithTag(saturdayCellTag).assertIsDisplayed()
		onNodeWithText("Sáb").assertIsDisplayed()
		onNodeWithText("1").assertIsDisplayed()
	}

	private fun cell(
		attemptId: String,
		code: String,
		blocks: IntRange
	) = ScheduleCellItem(
		attemptId = attemptId,
		codeText = code,
		classroomText = null,
		startBlock = blocks.first,
		endBlock = blocks.last,
		lane = 0,
		laneCount = 1
	)

	private companion object {
		const val LastBlock = 12
	}
}
