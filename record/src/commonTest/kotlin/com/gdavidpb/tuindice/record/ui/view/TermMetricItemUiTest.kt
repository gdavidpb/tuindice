package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.AnnotatedString
import com.gdavidpb.tuindice.record.presentation.model.TermMetricDelta
import com.gdavidpb.tuindice.record.presentation.model.TermMetricDeltaTone
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class TermMetricItemUiTest {
	@Test
	fun when_theMetricHasNothingToCompareWith_then_itsValueSitsRightAboveItsName() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermMetricItem(value = AnnotatedString("Δx 4.0000"), subtitle = "Trimestre")
		}

		val value = onNodeWithText("Δx 4.0000").assertIsDisplayed().getUnclippedBoundsInRoot()
		val subtitle = onNodeWithText("Trimestre").assertIsDisplayed().getUnclippedBoundsInRoot()

		assertTrue(value.bottom <= subtitle.top, "the value is read before what it measures")
		// The oldest term has no previous one: no change is shown, not even a zero.
		onAllNodesWithText("▲", substring = true).assertCountEquals(0)
		onAllNodesWithText("▼", substring = true).assertCountEquals(0)
	}

	@Test
	fun when_theMetricChangedSinceTheLastTerm_then_theChangeGoesBetweenTheValueAndItsName() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermMetricItem(
				value = AnnotatedString("∑x 3.7500"),
				delta = TermMetricDelta(text = "▲ 0.2500", tone = TermMetricDeltaTone.Positive),
				subtitle = "Acumulado"
			)
		}

		val value = onNodeWithText("∑x 3.7500").assertIsDisplayed().getUnclippedBoundsInRoot()
		val delta = onNodeWithText("▲ 0.2500").assertIsDisplayed().getUnclippedBoundsInRoot()
		val subtitle = onNodeWithText("Acumulado").assertIsDisplayed().getUnclippedBoundsInRoot()

		assertTrue(value.bottom <= delta.top, "the change follows the value it qualifies")
		assertTrue(delta.bottom <= subtitle.top, "and the name closes the metric")
	}

	@Test
	fun when_twoMetricsDifferOnlyInHavingAChange_then_theOneWithItIsTaller() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Row {
				TermMetricItem(
					modifier = Modifier.weight(1f),
					value = AnnotatedString("⦿ 12"),
					subtitle = "sin cambio"
				)
				TermMetricItem(
					modifier = Modifier.weight(1f),
					value = AnnotatedString("⦿ 15"),
					delta = TermMetricDelta(text = "▲ 3", tone = TermMetricDeltaTone.Informational),
					subtitle = "con cambio"
				)
			}
		}

		val plain = onNodeWithText("sin cambio").getUnclippedBoundsInRoot()
		val changed = onNodeWithText("con cambio").getUnclippedBoundsInRoot()

		// The chip takes a line of its own, so the name under it is pushed down.
		assertTrue(plain.bottom < changed.bottom, "the chip makes room for itself")
		assertTrue(plain.right <= changed.left, "each metric keeps to its half of the row")
	}
}
