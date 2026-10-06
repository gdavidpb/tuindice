package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.record.testing.assertDpEquals
import com.gdavidpb.tuindice.record.ui.model.ScheduleGridDefaults
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ScheduleBlockColumnUiTest {
	@Test
	fun when_theGridHasFourBlocks_then_theirNumbersAreListed_oneRowApart() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleBlockColumn(blockCount = 4)
		}

		(1..4).forEach { block -> onNodeWithText("$block").assertIsDisplayed() }
		onAllNodesWithText("0").assertCountEquals(0)
		onAllNodesWithText("5").assertCountEquals(0)

		// Each number is centred on its row, so two in a row sit a row and its gap apart: level with
		// the cells of their blocks.
		val rowStep = ScheduleGridDefaults.BlockRowHeight + ScheduleGridDefaults.BlockRowGap

		(1..3).forEach { block ->
			val top = onNodeWithText("$block").getUnclippedBoundsInRoot().top
			val nextTop = onNodeWithText("${block + 1}").getUnclippedBoundsInRoot().top

			assertDpEquals(expected = rowStep, actual = nextTop - top, what = "from block $block to the next")
		}
	}

	@Test
	fun when_theColumnIsMeasured_then_itIsAsTallAsItsRowsAndTheGapsBetweenThem() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleBlockColumn(modifier = Modifier.testTag(ColumnTag), blockCount = 4)
		}

		onNodeWithTag(ColumnTag)
			.assertWidthIsEqualTo(ScheduleGridDefaults.BlockColumnWidth)
			.assertHeightIsEqualTo(ScheduleGridDefaults.BlockRowHeight * 4 + ScheduleGridDefaults.BlockRowGap * 3)
	}

	private companion object {
		const val ColumnTag = "block_column"
	}
}
