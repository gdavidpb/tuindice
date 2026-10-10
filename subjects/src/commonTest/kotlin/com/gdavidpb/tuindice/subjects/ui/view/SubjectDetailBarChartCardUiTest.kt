package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SubjectDetailBarChartCardUiTest {
	@Test
	fun when_everyValueIsZero_then_displaysTitleAndNoDataMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailBarChartCard(
				title = "Intentos para aprobar",
				xValues = listOf(1, 2, 3),
				values = listOf(0, 0, 0),
				labelForX = { xValue -> xValue.toString() },
				overlay = { ChartOverlayMarker() }
			)
		}

		onNodeWithText("Intentos para aprobar").assertIsDisplayed()
		onNodeWithText("Sin datos suficientes").assertIsDisplayed()
		assertNodeHidden(OVERLAY_TAG)
	}

	@Test
	fun when_valuesAreEmpty_then_displaysNoDataMessage() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailBarChartCard(
				title = "Distribución de nota",
				xValues = emptyList(),
				values = emptyList(),
				labelForX = { xValue -> xValue.toString() },
				overlay = { ChartOverlayMarker() }
			)
		}

		onNodeWithText("Distribución de nota").assertIsDisplayed()
		onNodeWithText("Sin datos suficientes").assertIsDisplayed()
		assertNodeHidden(OVERLAY_TAG)
	}

	@Test
	fun when_anyValueIsPositive_then_rendersChartAreaWithOverlayInsteadOfNoData() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			SubjectDetailBarChartCard(
				title = "Intentos para aprobar",
				xValues = listOf(1, 2, 3),
				values = listOf(0, 5, 0),
				labelForX = { xValue -> xValue.toString() },
				overlay = { ChartOverlayMarker() }
			)
		}

		onNodeWithText("Intentos para aprobar").assertIsDisplayed()
		assertNodeVisible(OVERLAY_TAG)
		onAllNodesWithText("Sin datos suficientes").assertCountEquals(0)
	}
}

@Composable
private fun ChartOverlayMarker() {
	Box(
		modifier = Modifier
			.size(8.dp)
			.testTag(OVERLAY_TAG)
	)
}

private const val OVERLAY_TAG = "bar_chart_overlay"
