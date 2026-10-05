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
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptBadge
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptProjection
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.record.presentation.mapper.toScheduleItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.presentation.model.ScheduleGridItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
class ScheduleWeekViewUiTest {
	@Test
	fun when_theWeekIsTallerThanItsSpace_then_theBlocksScrollUnderTheDayNames() = runTuIndiceUiTest {
		val lastCellTag = RecordUiTags.scheduleCell("a2", ScheduleDay.Monday.code, LastBlock - 1)

		setTuIndiceTestContent {
			ScheduleWeekView(
				modifier = Modifier.height(200.dp),
				grid = grid(
					attempt(id = "a1", code = "CI5311", day = ScheduleDay.Monday, blocks = 1..2),
					attempt(id = "a2", code = "CI3725", day = ScheduleDay.Monday, blocks = LastBlock - 1..LastBlock),
					attempt(id = "a3", code = "EG1114")
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
		setTuIndiceTestContent {
			ScheduleWeekView(
				modifier = Modifier.width(200.dp),
				grid = grid(attempt(id = "a2", code = "FS2211", day = ScheduleDay.Saturday, blocks = 1..2))
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

	// Laid out by the mapper, so the cells are the ones the app draws: Monday to Friday always, and
	// a weekend day only when something meets then.
	private fun grid(vararg attempts: AttemptProjection): ScheduleGridItem =
		assertNotNull(attempts.toList().toScheduleItem()).grid

	private fun attempt(
		id: String,
		code: String,
		day: ScheduleDay? = null,
		blocks: IntRange = 1..1
	) = AttemptProjection(
		id = id,
		subjectCode = code,
		subjectName = code,
		credits = 3,
		gradingMode = AttemptGradingMode.NUMERIC,
		score = AttemptScore.empty(),
		outcome = AttemptOutcome.PENDING,
		badge = AttemptBadge.NONE,
		schedule = day?.let {
			listOf(
				AcademicScheduleEntry(
					dayOfWeek = day.code,
					startBlock = blocks.first,
					endBlock = blocks.last,
					classroom = ""
				)
			)
		}
	)

	private companion object {
		const val LastBlock = 12
	}
}
