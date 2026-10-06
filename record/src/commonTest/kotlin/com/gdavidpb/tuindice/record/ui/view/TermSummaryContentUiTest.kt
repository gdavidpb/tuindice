package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.record.presentation.model.TermMetricDelta
import com.gdavidpb.tuindice.record.presentation.model.TermMetricDeltaTone
import com.gdavidpb.tuindice.record.testing.termItem
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class TermSummaryContentUiTest {
	@Test
	fun when_aTermIsSummarized_then_itsThreeMetricsAreNamed_termThenCumulativeThenCredits() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermSummaryContent(item = termItem())
		}

		// Each value stands over the name of what it measures.
		val columns = listOf(
			"Δx 4.0000" to "Trimestre",
			"∑x 3.5000" to "Acumulado",
			"⦿ 12" to "UC inscritas"
		).map { (value, label) ->
			val valueBounds = onNodeWithText(value).assertIsDisplayed().getUnclippedBoundsInRoot()
			val labelBounds = onNodeWithText(label).assertIsDisplayed().getUnclippedBoundsInRoot()

			assertTrue(valueBounds.bottom <= labelBounds.top, "$value is read above $label")
			assertTrue(
				actual = valueBounds.left < labelBounds.right && labelBounds.left < valueBounds.right,
				message = "$value and $label share a column"
			)

			labelBounds
		}

		assertTrue(columns[0].right <= columns[1].left, "the term's average comes before the cumulative one")
		assertTrue(columns[1].right <= columns[2].left, "and the credits close the row")
		// With nothing to compare against, no change is shown under any of them.
		onAllNodesWithText("▲", substring = true).assertCountEquals(0)
		onAllNodesWithText("▼", substring = true).assertCountEquals(0)
	}

	@Test
	fun when_theTermHasAPreviousOne_then_eachMetricShowsItsOwnChange_inItsOwnColumn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermSummaryContent(
				item = termItem().copy(
					gradeDelta = TermMetricDelta(text = "▲ 0.5000", tone = TermMetricDeltaTone.Positive),
					gradeSumDelta = TermMetricDelta(text = "▼ 0.1250", tone = TermMetricDeltaTone.Negative),
					creditsDelta = TermMetricDelta(text = "▼ 3", tone = TermMetricDeltaTone.Informational)
				)
			)
		}

		listOf(
			"▲ 0.5000" to "Trimestre",
			"▼ 0.1250" to "Acumulado",
			"▼ 3" to "UC inscritas"
		).forEach { (delta, label) ->
			val deltaBounds = onNodeWithText(delta).assertIsDisplayed().getUnclippedBoundsInRoot()
			val labelBounds = onNodeWithText(label).assertIsDisplayed().getUnclippedBoundsInRoot()

			assertTrue(deltaBounds.bottom <= labelBounds.top, "$delta is read above $label")
			assertTrue(
				actual = labelBounds.left <= deltaBounds.left && deltaBounds.right <= labelBounds.right,
				message = "$delta belongs to the column of $label"
			)
		}
	}

	@Test
	fun when_onlyOneMetricChanged_then_theOthersShowNoChange_andAllNamesStayOnOneLine() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermSummaryContent(
				item = termItem().copy(
					creditsDelta = TermMetricDelta(text = "▲ 4", tone = TermMetricDeltaTone.Informational)
				)
			)
		}

		onNodeWithText("▲ 4").assertIsDisplayed()
		onAllNodesWithText("▼", substring = true).assertCountEquals(0)

		// The row centres its columns, so a chip in one of them does not drop the others' texts out.
		onNodeWithText("Trimestre").assertIsDisplayed()
		onNodeWithText("Acumulado").assertIsDisplayed()
		onNodeWithText("UC inscritas").assertIsDisplayed()
	}
}
