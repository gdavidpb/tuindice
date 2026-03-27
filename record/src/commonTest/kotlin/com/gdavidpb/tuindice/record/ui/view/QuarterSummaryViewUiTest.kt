package com.gdavidpb.tuindice.record.ui.view

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

@OptIn(ExperimentalTestApi::class)
class QuarterSummaryViewUiTest {
	@Test
	fun when_summaryIsRendered_then_displaysMetricsAndLabels() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			QuarterSummaryView(
				item = sampleQuarterItem(
					gradeDelta = sampleQuarterMetricDelta(text = "+0.50"),
					gradeSumDelta = sampleQuarterMetricDelta(
						text = "-0.25",
						tone = QuarterMetricDeltaTone.Negative
					)
				)
			)
		}

		onNodeWithText("Δx 4.2500").assertIsDisplayed()
		onNodeWithText("∑x 4.2500").assertIsDisplayed()
		onNodeWithText("⦿ 6").assertIsDisplayed()
		onNodeWithText("+0.50").assertIsDisplayed()
		onNodeWithText("-0.25").assertIsDisplayed()
		onNodeWithText("Trimestre").assertIsDisplayed()
		onNodeWithText("Acumulado").assertIsDisplayed()
		onNodeWithText("UC inscritas").assertIsDisplayed()
		onAllNodesWithText("Actual").assertCountEquals(0)
	}
}
