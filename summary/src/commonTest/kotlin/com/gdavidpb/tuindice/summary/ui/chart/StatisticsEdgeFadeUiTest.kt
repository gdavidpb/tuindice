package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class StatisticsEdgeFadeUiTest {
	@Test
	fun when_theLeadingEdgeIsFaded_then_theFadeFillsItsEdgeTheFullHeight() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			FadeHost(alignment = Alignment.CenterStart)
		}
		waitForIdle()

		val fade = boundsOf(FADE_TAG)
		assertEquals(StatisticsChartDefaults.EdgeFadeWidth.value, fade.width, 1f)
		assertEquals(boundsOf(HOST_TAG).height, fade.height, 1f)
		assertEquals(boundsOf(HOST_TAG).left, fade.left, 1f)
	}

	@Test
	fun when_theTrailingEdgeIsFaded_then_theFadeSitsAtTheEnd() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			FadeHost(alignment = Alignment.CenterEnd)
		}
		waitForIdle()

		assertEquals(boundsOf(HOST_TAG).right, boundsOf(FADE_TAG).right, 1f)
	}

	@Composable
	private fun FadeHost(alignment: Alignment) {
		Box(
			modifier = Modifier
				.testTag(HOST_TAG)
				.width(WIDTH_DP.dp)
				.height(HEIGHT_DP.dp)
		) {
			StatisticsEdgeFade(
				alignment = alignment,
				brush = Brush.horizontalGradient(colors = listOf(Color.Gray, Color.Transparent)),
				tag = FADE_TAG
			)
		}
	}

	private fun ComposeUiTest.boundsOf(tag: String) =
		onNodeWithTag(testTag = tag, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot

	private companion object {
		const val HOST_TAG = "summary_statistics_fade_host"
		const val FADE_TAG = "summary_statistics_fade"
		const val WIDTH_DP = 200
		const val HEIGHT_DP = 40
	}
}
