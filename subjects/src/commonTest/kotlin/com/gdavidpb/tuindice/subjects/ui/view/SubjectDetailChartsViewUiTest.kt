package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectDetailItem
import com.gdavidpb.tuindice.subjects.testing.emptySubjectSegmentItem
import com.gdavidpb.tuindice.subjects.testing.subjectDetailItem
import com.gdavidpb.tuindice.subjects.testing.subjectSegmentItem
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SubjectDetailChartsViewUiTest {
	@Test
	fun when_chartModeIsNumericGrades_then_displaysGradeDistributionChart() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailChartsView(
				detail = subjectDetailItem().copy(
					chartMode = SubjectDetailItem.ChartMode.NUMERIC_GRADES
				),
				segment = subjectSegmentItem()
			)
		}

		assertNodeVisible(SubjectsUiTags.Charts)
		onNodeWithText("Distribución de nota").assertIsDisplayed()
		onNodeWithText("Intentos para aprobar").assertIsDisplayed()
		onAllNodesWithText("Distribución de resultado").assertCountEquals(0)
		onAllNodesWithText("Sin datos suficientes").assertCountEquals(0)
	}

	@Test
	fun when_chartModeIsQualitativeOutcomes_then_displaysOutcomeDistributionChart() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailChartsView(
				detail = subjectDetailItem().copy(
					chartMode = SubjectDetailItem.ChartMode.QUALITATIVE_OUTCOMES
				),
				segment = subjectSegmentItem()
			)
		}

		onNodeWithText("Distribución de resultado").assertIsDisplayed()
		onNodeWithText("Intentos para aprobar").assertIsDisplayed()
		onAllNodesWithText("Distribución de nota").assertCountEquals(0)
		onAllNodesWithText("Sin datos suficientes").assertCountEquals(0)
	}

	@Test
	fun when_segmentHasNoCounts_then_bothChartsDisplayNoDataMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailChartsView(
				detail = subjectDetailItem().copy(
					chartMode = SubjectDetailItem.ChartMode.QUALITATIVE_OUTCOMES
				),
				segment = emptySubjectSegmentItem()
			)
		}

		onAllNodesWithText("Sin datos suficientes").assertCountEquals(2)
	}

	@Test
	fun when_attemptBucketsAreUnknown_then_onlyAttemptsChartDisplaysNoData() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailChartsView(
				detail = subjectDetailItem().copy(
					chartMode = SubjectDetailItem.ChartMode.NUMERIC_GRADES
				),
				segment = subjectSegmentItem().copy(
					attemptsToPassBins = listOf(
						SubjectDetailItem.AttemptBinItem(bucket = "4", count = 9)
					)
				)
			)
		}

		onNodeWithText("Distribución de nota").assertIsDisplayed()
		onAllNodesWithText("Sin datos suficientes").assertCountEquals(1)
	}
}
