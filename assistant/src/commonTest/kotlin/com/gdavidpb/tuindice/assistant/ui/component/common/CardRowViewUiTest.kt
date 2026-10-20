package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CardRowViewUiTest {
	@Test
	fun when_aRowHasACodeATitleAndAName_then_eachIsShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CardRowView(code = "CI5311", title = "Reglamento de Pregrado", name = "Artículo 12", showsDivider = true)
		}
		waitForIdle()

		onNodeWithText("CI5311").assertIsDisplayed()
		onNodeWithText("Reglamento de Pregrado").assertIsDisplayed()
		onNodeWithText("Artículo 12").assertIsDisplayed()
	}

	@Test
	fun when_theTrailingIsShort_then_theNameKeepsMostOfTheRow() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(modifier = Modifier.width(ROW_WIDTH_DP.dp).testTag(ROW_TAG)) {
				CardRowView(name = LONG_NAME, trailing = { CardValueView(text = "3 UC") })
			}
		}
		waitForIdle()

		val row = onNodeWithTag(ROW_TAG).fetchSemanticsNode().size.width
		val name = onNodeWithText(LONG_NAME).fetchSemanticsNode().size.width

		// The trailing side is a figure of a few letters, so the name takes what is left of the row, not half of it.
		assertTrue(name > row * 0.6f, "the name is ${name}px of a ${row}px row")
	}

	@Test
	fun when_aNameIsLongerThanARow_then_itWrapsOntoEveryLineAndIsNotCut() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(modifier = Modifier.width(NARROW_WIDTH_DP.dp)) {
				CardRowView(name = LONG_NAME)
			}
		}
		waitForIdle()

		val layout = onNodeWithText(LONG_NAME).textLayout()

		assertTrue(layout.lineCount >= 3, "the name takes ${layout.lineCount} lines")
		assertFalse(layout.hasVisualOverflow, "the name is cut")
	}

	@Test
	fun when_aRowHasSeveralCodes_then_eachIsAChip() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CardRowView(codes = listOf("IS-101", "IS-102"), name = "Miércoles · bloque 3")
		}
		waitForIdle()

		onNodeWithText("IS-101").assertIsDisplayed()
		onNodeWithText("IS-102").assertIsDisplayed()
		onNodeWithText("Miércoles · bloque 3").assertIsDisplayed()
	}

	@Test
	fun when_aRowIsPlain_then_itIsNotATap() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CardRowView(name = "Artículo 12")
		}
		waitForIdle()

		onNodeWithText("Artículo 12").assertHasNoClickAction()
	}

	// The layout of a text as it is drawn: how many lines it takes, and whether any of it is cut.
	private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
		val layouts = mutableListOf<TextLayoutResult>()
		fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)

		return layouts.single()
	}

	private companion object {
		const val ROW_TAG = "card_row"
		const val ROW_WIDTH_DP = 300
		const val NARROW_WIDTH_DP = 120
		const val LONG_NAME = "Programación orientada a objetos avanzada, con prácticas de laboratorio"
	}
}
