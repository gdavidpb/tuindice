package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SubjectDetailSegmentSummaryViewUiTest {
	@Test
	fun when_rendered_then_showsBothMetricsWithAccessibleDescriptions() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailSegmentSummaryView(
				studentsText = "2.4k",
				attemptsText = "3.7k"
			)
		}

		onNodeWithText("2.4k").assertIsDisplayed()
		onNodeWithText("3.7k").assertIsDisplayed()
		onNodeWithTag(SubjectsUiTags.SegmentStudentsMetric)
			.assertContentDescriptionContains("2.4k estudiantes que cursaron esta materia")
		onNodeWithTag(SubjectsUiTags.SegmentAttemptsMetric)
			.assertContentDescriptionContains("3.7k veces que fue cursada esta materia")
	}

	@Test
	fun when_studentsMetricLongPressed_then_showsTooltipInTwoLines() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailSegmentSummaryView(
				studentsText = "2.4k",
				attemptsText = "3.7k"
			)
		}

		onNodeWithTag(SubjectsUiTags.SegmentStudentsMetric)
			.performTouchInput { longClick() }

		onNodeWithText("Cantidad de estudiantes").assertIsDisplayed()
		onNodeWithText("que cursaron esta materia").assertIsDisplayed()
	}
}
