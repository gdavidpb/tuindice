package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsOutcome
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsLegendViewUiTest {
	@Test
	fun when_segmentsAreGiven_then_eachLegendIsWritten() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsLegendView(
				segments = listOf(
					segmentOf(SummaryStatisticsOutcome.APPROVED, 44f, "44 aprobadas", "44 aprobadas · 94 %"),
					segmentOf(SummaryStatisticsOutcome.FAILED, 2f, "2 reprobadas", "2 reprobadas · 4 %"),
					segmentOf(SummaryStatisticsOutcome.WITHDRAWN, 1f, "1 retirada", "1 retirada · 2 %")
				)
			)
		}

		onNodeWithText("44 aprobadas").assertIsDisplayed()
		onNodeWithText("2 reprobadas").assertIsDisplayed()
		onNodeWithText("1 retirada").assertIsDisplayed()
	}

	@Test
	fun when_aSegmentHasNoCount_then_itIsStillListedInOrder() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsLegendView(
				segments = listOf(
					segmentOf(SummaryStatisticsOutcome.APPROVED, 3f, "3 aprobadas", "3 aprobadas · 100 %"),
					segmentOf(SummaryStatisticsOutcome.WITHDRAWN, 0f, "0 retiradas", "0 retiradas · 0 %")
				)
			)
		}

		onNodeWithText("0 retiradas").assertIsDisplayed()
		val approved = onNodeWithText("3 aprobadas").fetchSemanticsNode().boundsInRoot
		val withdrawn = onNodeWithText("0 retiradas").fetchSemanticsNode().boundsInRoot
		assertTrue(approved.left < withdrawn.left)
	}
}
