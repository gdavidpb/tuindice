package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class StatisticsReferenceLineUiTest {
	@Test
	fun when_aReferenceIsGiven_then_itsLabelIsWrittenAboveTheLineAtItsEnd() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ReferenceHost(y = 60f)
		}
		waitForIdle()

		onNodeWithTag(SummaryUiTags.StatisticsReferenceLabel, useUnmergedTree = true).assertTextEquals("promedio 4.2")
		assertEquals(
			60f - StatisticsChartDefaults.ReferenceLabelHeight.value,
			boundsOf(SummaryUiTags.StatisticsReferenceLabel).top - boundsOf(HOST_TAG).top,
			1f
		)
	}

	@Test
	fun when_aReferenceLabelIsWritten_then_itStaysClearOfTheEdgeFade() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ReferenceHost(y = 60f)
		}
		waitForIdle()

		// The label ends where the edge fade begins, so the fade never hides its last word.
		val fadeStart = boundsOf(HOST_TAG).right - StatisticsChartDefaults.EdgeFadeWidth.value
		assertTrue(boundsOf(SummaryUiTags.StatisticsReferenceLabel).right <= fadeStart + 1f)
	}

	@Composable
	private fun ReferenceHost(y: Float) {
		Box(
			modifier = Modifier
				.testTag(HOST_TAG)
				.width(WIDTH_DP.dp)
				.height(StatisticsChartDefaults.ChartHeight)
		) {
			StatisticsReferenceLine(label = "promedio 4.2", y = y, widthDp = WIDTH_DP.toFloat())
		}
	}

	private fun ComposeUiTest.boundsOf(tag: String) =
		onNodeWithTag(testTag = tag, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

	private companion object {
		const val HOST_TAG = "summary_statistics_reference_host"
		const val WIDTH_DP = 200
	}
}
