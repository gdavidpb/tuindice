package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.presentation.mapper.localizedShortWeekdayNames
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
		val labels = localizedShortWeekdayNames()

		setTuIndiceTestContent {
			WeekdayHeaderRow()
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationWeekdayHeaderRow)
		labels.forEach { label ->
			onNodeWithText(label).assertIsDisplayed()
		}
	}

	@Test
	fun when_rendered_then_containsExactlySevenWeekdayLabels() = runTuIndiceUiTest {
		val labels = localizedShortWeekdayNames()

		setTuIndiceTestContent {
			WeekdayHeaderRow()
		}

		assertEquals(7, labels.size)
		labels.forEach { label ->
			onAllNodesWithText(label).assertCountEquals(1)
		}
	}
}
