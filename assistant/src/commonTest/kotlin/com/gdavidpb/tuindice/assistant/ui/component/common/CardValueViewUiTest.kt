package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CardValueViewUiTest {
	@Test
	fun when_aMutedSuffixIsGiven_then_onlyTheSuffixIsMutedAndAtTheRegularWeight() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CardValueView(text = TEXT, mutedSuffix = SUFFIX)
		}
		waitForIdle()

		onNodeWithText(TEXT).assertIsDisplayed()
		val text = onNodeWithText(TEXT).fetchSemanticsNode().config[SemanticsProperties.Text].single()
		val muted = text.spanStyles.single()

		assertEquals(TEXT.length - SUFFIX.length, muted.start)
		assertEquals(TEXT.length, muted.end)
		assertEquals(FontWeight.Normal, muted.item.fontWeight)
		assertTrue(abs(muted.item.color.alpha - TuIndiceAlpha.Deemphasis) < ALPHA_TOLERANCE)
	}

	@Test
	fun when_theTextDoesNotEndWithTheSuffix_then_nothingIsMuted() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CardValueView(text = TEXT, mutedSuffix = "9:00 a. m.")
		}
		waitForIdle()

		val text = onNodeWithText(TEXT).fetchSemanticsNode().config[SemanticsProperties.Text].single()

		assertTrue(text.spanStyles.isEmpty())
	}

	private companion object {
		const val TEXT = "1-2 · 7:30–9:30 a. m."
		const val SUFFIX = "7:30–9:30 a. m."

		// A color keeps its alpha in eight bits, so a token comes back within 1/255 of its value.
		const val ALPHA_TOLERANCE = 0.01f
	}
}
