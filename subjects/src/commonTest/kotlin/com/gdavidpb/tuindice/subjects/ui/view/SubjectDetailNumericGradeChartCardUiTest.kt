package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectDetailItem
import com.gdavidpb.tuindice.subjects.testing.subjectSegmentItem
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SubjectDetailNumericGradeChartCardUiTest {
	@Test
	fun when_segmentHasGradeBins_then_displaysGradeDistributionChart() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailNumericGradeChartCard(
				segment = subjectSegmentItem().copy(
					medianGrade = 4.0,
					stddevGrade = 0.8,
					latestGradeBins = listOf(
						SubjectDetailItem.GradeBinItem(grade = 1, count = 1),
						SubjectDetailItem.GradeBinItem(grade = 5, count = 3)
					)
				)
			)
		}

		onNodeWithText("Distribución de nota").assertIsDisplayed()
		onAllNodesWithText("Sin datos suficientes").assertCountEquals(0)
	}

	@Test
	fun when_segmentHasNoGradeBins_then_displaysNoDataMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailNumericGradeChartCard(
				segment = subjectSegmentItem().copy(
					medianGrade = null,
					stddevGrade = null,
					latestGradeBins = emptyList()
				)
			)
		}

		onNodeWithText("Distribución de nota").assertIsDisplayed()
		onNodeWithText("Sin datos suficientes").assertIsDisplayed()
	}

	@Test
	fun when_gradeBinsAreOutsideTheScale_then_displaysNoDataMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailNumericGradeChartCard(
				segment = subjectSegmentItem().copy(
					latestGradeBins = listOf(
						SubjectDetailItem.GradeBinItem(grade = 0, count = 7),
						SubjectDetailItem.GradeBinItem(grade = 6, count = 4)
					)
				)
			)
		}

		onNodeWithText("Sin datos suficientes").assertIsDisplayed()
	}

	@Test
	fun when_medianAndStddevAreMissing_then_stillDisplaysChartForGradeBins() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailNumericGradeChartCard(
				segment = subjectSegmentItem().copy(
					medianGrade = null,
					stddevGrade = null,
					latestGradeBins = listOf(
						SubjectDetailItem.GradeBinItem(grade = 3, count = 2)
					)
				)
			)
		}

		onNodeWithText("Distribución de nota").assertIsDisplayed()
		onAllNodesWithText("Sin datos suficientes").assertCountEquals(0)
	}
}
