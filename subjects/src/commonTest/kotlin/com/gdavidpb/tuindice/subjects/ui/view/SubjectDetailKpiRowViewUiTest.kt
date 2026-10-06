package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.subjects.testing.emptySubjectSegmentItem
import com.gdavidpb.tuindice.subjects.testing.subjectSegmentItem
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SubjectDetailKpiRowViewUiTest {
	@Test
	fun when_rendered_then_displaysEveryKpiTitle() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailKpiRowView(segment = subjectSegmentItem())
		}

		onNodeWithText("Score de dificultad").assertIsDisplayed()
		onNodeWithText("Primer intento").assertIsDisplayed()
		onNodeWithText("Aprobación").assertIsDisplayed()
		onNodeWithText("Reprobación").assertIsDisplayed()
		onNodeWithText("Retiro").assertIsDisplayed()
	}

	@Test
	fun when_segmentHasMetrics_then_displaysEachSegmentValue() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailKpiRowView(
				segment = subjectSegmentItem().copy(
					difficultyScoreText = "41 / 100",
					difficultyBandText = "Media",
					firstAttemptPassRateText = "55%",
					approvalRateText = "72%",
					failureRateText = "22%",
					withdrawalRateText = "6%"
				)
			)
		}

		onNodeWithText("41 / 100").assertIsDisplayed()
		onNodeWithText("Media").assertIsDisplayed()
		onNodeWithText("55%").assertIsDisplayed()
		onNodeWithText("72%").assertIsDisplayed()
		onNodeWithText("22%").assertIsDisplayed()
		onNodeWithText("6%").assertIsDisplayed()
	}

	@Test
	fun when_segmentMetricsAreEmpty_then_displaysPlaceholderInEveryKpi() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailKpiRowView(segment = emptySubjectSegmentItem())
		}

		onAllNodesWithText("--").assertCountEquals(6)
	}
}
