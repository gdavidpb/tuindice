package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.presentation.model.CardChipItem
import com.gdavidpb.tuindice.assistant.presentation.model.CardChipTone
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CardTrailingViewUiTest {
	@Test
	fun when_aFigureAndChipsAreShown_then_theFigureComesFirstAndTheChipsAfterIt() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CardTrailingView(
				value = "4.00",
				chips = listOf(CardChipItem(UiText.Raw("En curso"), CardChipTone.Current))
			)
		}
		waitForIdle()

		onNodeWithText("4.00").assertIsDisplayed()
		onNodeWithText("En curso").assertIsDisplayed()
		val figureLeft = onNodeWithText("4.00").fetchSemanticsNode().boundsInRoot.left
		val chipLeft = onNodeWithText("En curso").fetchSemanticsNode().boundsInRoot.left
		assertTrue(figureLeft < chipLeft, "the figure starts at $figureLeft and the chip at $chipLeft")
	}

	@Test
	fun when_theChipsDoNotFitBesideTheFigure_then_theyWrapOntoASecondLine() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(modifier = Modifier.width(NARROW_WIDTH_DP.dp)) {
				CardTrailingView(
					value = "12.50 / 60",
					chips = listOf(CardChipItem(UiText.Raw("Aprobada por equivalencia"), CardChipTone.Approved))
				)
			}
		}
		waitForIdle()

		val figureTop = onNodeWithText("12.50 / 60").fetchSemanticsNode().boundsInRoot.top
		val chipTop = onNodeWithText("Aprobada por equivalencia").fetchSemanticsNode().boundsInRoot.top
		assertTrue(chipTop > figureTop, "the chip is at ${chipTop}px, under the figure at ${figureTop}px")
	}

	private companion object {
		const val NARROW_WIDTH_DP = 120
	}
}
