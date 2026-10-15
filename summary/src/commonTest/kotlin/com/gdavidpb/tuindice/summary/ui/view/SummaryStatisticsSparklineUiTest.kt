package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsSparklineUiTest {
	@Test
	fun when_theAveragesAreGiven_then_theLineIsAsTallAsItsBand() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsSparkline(values = listOf(4.1f, 4.2f, 4.5f))
		}
		waitForIdle()

		val bounds = onNodeWithTag(SummaryUiTags.StatisticsSparkline, useUnmergedTree = true)
			.fetchSemanticsNode()
			.boundsInRoot
		assertEquals(SPARKLINE_HEIGHT_PX, bounds.height, 1f)
	}

	@Test
	fun when_aSingleAverageIsGiven_then_itIsStillDrawnAsTheLine() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SummaryStatisticsSparkline(values = listOf(4.5f))
		}
		waitForIdle()

		assertNodeVisible(SummaryUiTags.StatisticsSparkline, useUnmergedTree = true)
	}

	private companion object {
		// The height of the line is 44 dp, which is 44 pixels at the density of the test.
		const val SPARKLINE_HEIGHT_PX = 44f
	}
}
