package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.record.presentation.model.QuarterMetricDeltaTone
import com.gdavidpb.tuindice.record.testing.sampleQuarterMetricDelta
import com.gdavidpb.tuindice.record.testing.sampleQuarterItem
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class QuarterSummaryViewUiTest {
	@Test
	fun when_deltaToneIsNeutral_then_usesInformationalChipColors() {
		val primaryContainer = Color(0xFF333333)
		val onPrimaryContainer = Color(0xFF444444)

		val neutralColors = quarterDeltaChipColors(
			tone = QuarterMetricDeltaTone.Neutral,
			primaryContainer = primaryContainer,
			onPrimaryContainer = onPrimaryContainer
		)
		val informationalColors = quarterDeltaChipColors(
			tone = QuarterMetricDeltaTone.Informational,
			primaryContainer = primaryContainer,
			onPrimaryContainer = onPrimaryContainer
		)

		assertEquals(informationalColors, neutralColors)
	}

	@Test
	fun when_summaryIsRendered_then_displaysMetricsAndLabels() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			QuarterSummaryView(
				item = sampleQuarterItem(
					gradeDelta = sampleQuarterMetricDelta(text = "▲ 0.5000"),
					gradeSumDelta = sampleQuarterMetricDelta(
						text = "▼ 0.2500",
						tone = QuarterMetricDeltaTone.Negative
					),
					creditsDelta = sampleQuarterMetricDelta(
						text = "▲ 2",
						tone = QuarterMetricDeltaTone.Informational
					)
				)
			)
		}

		onNodeWithText("Δx 4.2500").assertIsDisplayed()
		onNodeWithText("∑x 4.2500").assertIsDisplayed()
		onNodeWithText("⦿ 6").assertIsDisplayed()
		onNodeWithText("▲ 0.5000").assertIsDisplayed()
		onNodeWithText("▼ 0.2500").assertIsDisplayed()
		onNodeWithText("▲ 2").assertIsDisplayed()
		onNodeWithText("Trimestre").assertIsDisplayed()
		onNodeWithText("Acumulado").assertIsDisplayed()
		onNodeWithText("UC inscritas").assertIsDisplayed()
		onAllNodesWithText("Actual").assertCountEquals(0)
	}
}
