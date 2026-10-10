package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class WeekdayHeaderRowUiTest {
	@Test
	fun when_rendered_then_displaysLocalizedWeekdayLabels() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WeekdayHeaderRow()
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationWeekdayHeaderRow)
		WeekdayLabels.forEach { label ->
			onNodeWithText(label).assertIsDisplayed()
		}
	}

	@Test
	fun when_rendered_then_containsExactlySevenWeekdayLabels() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WeekdayHeaderRow()
		}

		assertEquals(7, WeekdayLabels.size)
		WeekdayLabels.forEach { label ->
			onAllNodesWithText(label).assertCountEquals(1)
		}
	}

	@Test
	fun when_rendered_then_ordersTheWeekFromMondayToSunday() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			WeekdayHeaderRow()
		}

		val lefts = WeekdayLabels.map { label ->
			onNodeWithText(label).getUnclippedBoundsInRoot().left
		}

		assertEquals(lefts.sorted(), lefts)
		assertEquals(WeekdayLabels.size, lefts.distinct().size)
	}

	private companion object {
		// The row reads them from the resources: short, lower case, Monday first.
		val WeekdayLabels = listOf("lun", "mar", "mié", "jue", "vie", "sáb", "dom")
	}
}
