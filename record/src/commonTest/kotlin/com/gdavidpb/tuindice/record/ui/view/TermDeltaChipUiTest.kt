package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.record.presentation.model.TermMetricDelta
import com.gdavidpb.tuindice.record.presentation.model.TermMetricDeltaTone
import com.gdavidpb.tuindice.record.testing.textLayout
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalTestApi::class)
class TermDeltaChipUiTest {
	@Test
	fun when_theMetricWentUp_then_theChipShowsTheRise_inGreen() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermDeltaChip(delta = TermMetricDelta(text = "▲ 0.5000", tone = TermMetricDeltaTone.Positive))
		}

		onNodeWithText("▲ 0.5000")
			.assertIsDisplayed()
			.assertHasNoClickAction()
		assertEquals(PositiveContent, onNodeWithText("▲ 0.5000").textLayout().layoutInput.style.color)
	}

	@Test
	fun when_theMetricWentDown_then_theChipShowsTheFall_inRed() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TermDeltaChip(delta = TermMetricDelta(text = "▼ 0.2500", tone = TermMetricDeltaTone.Negative))
		}

		onNodeWithText("▼ 0.2500").assertIsDisplayed()
		assertEquals(NegativeContent, onNodeWithText("▼ 0.2500").textLayout().layoutInput.style.color)
	}

	@Test
	fun when_theChangeIsNeitherGoodNorBad_then_theChipKeepsTheThemesOwnColors() = runTuIndiceUiTest {
		var onPrimaryContainer = Color.Unspecified

		setTuIndiceTestContent {
			onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer

			Column {
				// An average that did not move, and credits: taking more or fewer is not a verdict.
				TermDeltaChip(delta = TermMetricDelta(text = "0.0000", tone = TermMetricDeltaTone.Neutral))
				TermDeltaChip(delta = TermMetricDelta(text = "▼ 3", tone = TermMetricDeltaTone.Informational))
			}
		}

		listOf("0.0000", "▼ 3").forEach { text ->
			val color = onNodeWithText(text).assertIsDisplayed().textLayout().layoutInput.style.color

			assertEquals(onPrimaryContainer, color, "the colour of \"$text\"")
			assertNotEquals(PositiveContent, color)
			assertNotEquals(NegativeContent, color)
		}
	}

	@Test
	fun when_theToneIsResolved_then_onlyAVerdictGetsAColorOfItsOwn() = runTuIndiceUiTest {
		val themed = TermDeltaChipColors(containerColor = Color.Blue, contentColor = Color.White)

		fun colorsOf(tone: TermMetricDeltaTone) = termDeltaChipColors(
			tone = tone,
			primaryContainer = themed.containerColor,
			onPrimaryContainer = themed.contentColor
		)

		assertEquals(themed, colorsOf(TermMetricDeltaTone.Neutral))
		assertEquals(themed, colorsOf(TermMetricDeltaTone.Informational))
		assertEquals(PositiveContent, colorsOf(TermMetricDeltaTone.Positive).contentColor)
		assertEquals(NegativeContent, colorsOf(TermMetricDeltaTone.Negative).contentColor)
		// A rise and a fall are told apart by the fill as well as by the text.
		assertNotEquals(
			illegal = colorsOf(TermMetricDeltaTone.Positive).containerColor,
			actual = colorsOf(TermMetricDeltaTone.Negative).containerColor
		)
	}

	private companion object {
		val PositiveContent = Color(0xFF479A21)
		val NegativeContent = Color(0xFF9A212D)
	}
}
