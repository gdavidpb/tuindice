package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
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

	private companion object {
		const val ROW_TAG = "card_row"
		const val ROW_WIDTH_DP = 300
		const val LONG_NAME = "Programación orientada a objetos avanzada, con prácticas de laboratorio"
	}
}
