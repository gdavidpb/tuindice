package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ScheduleTableEvaluationLegendViewUiTest {
	@Test
	fun when_itIsShown_then_itSaysWhatTheEvaluationIconMeans() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleTableEvaluationLegendView()
		}

		assertNodeVisible(RecordUiTags.ScheduleEvaluationLegend)
		onNodeWithText("Evaluación esta semana").assertIsDisplayed()
	}

	@Test
	fun when_itIsShown_then_theIconComesBeforeTheText_onTheSameLine() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ScheduleTableEvaluationLegendView()
		}

		val legend = onNodeWithTag(RecordUiTags.ScheduleEvaluationLegend).fetchSemanticsNode()
		val text = onNodeWithText("Evaluación esta semana").fetchSemanticsNode()

		// The legend is the row (icon and text); the text starts after the icon, not under it.
		assertTrue(text.positionInRoot.x > legend.positionInRoot.x)
		assertTrue(text.boundsInRoot.top >= legend.boundsInRoot.top)
		assertTrue(text.boundsInRoot.bottom <= legend.boundsInRoot.bottom)
	}
}
