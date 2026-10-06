package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.subjects_segment_attempts_tooltip_line_1
import tuindice.subjects.generated.resources.subjects_segment_attempts_tooltip_line_2
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SubjectDetailSummaryMetricUiTest {
	@Test
	fun when_rendered_then_displaysValueIconAndContentDescription() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailSummaryMetric(
				testTag = SubjectsUiTags.SegmentAttemptsMetric,
				valueText = "3.7k",
				contentDescription = "3.7k veces que fue cursada esta materia",
				tooltipLine1 = Res.string.subjects_segment_attempts_tooltip_line_1,
				tooltipLine2 = Res.string.subjects_segment_attempts_tooltip_line_2
			) {
				Text(
					modifier = Modifier.testTag(ICON_TAG),
					text = "icono"
				)
			}
		}

		onNodeWithText("3.7k", useUnmergedTree = true).assertIsDisplayed()
		assertNodeVisible(tag = ICON_TAG, useUnmergedTree = true)
		onNodeWithTag(SubjectsUiTags.SegmentAttemptsMetric)
			.assertContentDescriptionEquals("3.7k veces que fue cursada esta materia")
	}

	@Test
	fun when_notClicked_then_tooltipLinesAreHidden() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailSummaryMetric(
				testTag = SubjectsUiTags.SegmentAttemptsMetric,
				valueText = "3.7k",
				contentDescription = "3.7k veces que fue cursada esta materia",
				tooltipLine1 = Res.string.subjects_segment_attempts_tooltip_line_1,
				tooltipLine2 = Res.string.subjects_segment_attempts_tooltip_line_2
			) {}
		}

		assertNodeVisible(SubjectsUiTags.SegmentAttemptsMetric)
		onAllNodesWithText("Cantidad de veces").assertCountEquals(0)
		onAllNodesWithText("que fue cursada esta materia").assertCountEquals(0)
	}

	@Test
	fun when_metricClicked_then_showsBothTooltipLines() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailSummaryMetric(
				testTag = SubjectsUiTags.SegmentAttemptsMetric,
				valueText = "3.7k",
				contentDescription = "3.7k veces que fue cursada esta materia",
				tooltipLine1 = Res.string.subjects_segment_attempts_tooltip_line_1,
				tooltipLine2 = Res.string.subjects_segment_attempts_tooltip_line_2
			) {}
		}

		onNodeWithTag(SubjectsUiTags.SegmentAttemptsMetric).performClick()

		onNodeWithText("Cantidad de veces").assertIsDisplayed()
		onNodeWithText("que fue cursada esta materia").assertIsDisplayed()
	}

	private companion object {
		const val ICON_TAG = "summary_metric_icon"
	}
}
