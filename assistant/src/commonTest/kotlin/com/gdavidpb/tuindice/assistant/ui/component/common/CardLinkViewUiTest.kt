package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CardLinkViewUiTest {
	@Test
	fun when_aLinkHasAScreenReaderLabel_then_itAnnouncesWhatTheTapOpens() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CardLinkView(text = "Ver materia", onClick = {}, onClickLabel = "Ver materia IS-101")
		}
		waitForIdle()

		onNodeWithText("Ver materia").assertIsDisplayed().assertHasClickAction()
		val onClick = onNodeWithText("Ver materia").fetchSemanticsNode().config[SemanticsActions.OnClick]
		assertEquals("Ver materia IS-101", onClick.label)
	}

	@Test
	fun when_aLinkThatLeavesTheAppIsTapped_then_itRunsItsAction() = runTuIndiceUiTest {
		var taps = 0
		setTuIndiceTestContent {
			CardLinkView(text = "Ver documento oficial", onClick = { taps++ }, external = true)
		}
		waitForIdle()

		onNodeWithText("Ver documento oficial").performClick()
		assertEquals(1, taps)
	}

	@Test
	fun when_aCardLinkIsShown_then_itEndsTheCardOnTheRight() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			Box(modifier = Modifier.width(CARD_WIDTH_DP.dp).testTag(CARD_TAG)) {
				CardLinkView(text = "Ver en Informe", onClick = {})
			}
		}
		waitForIdle()

		val card = onNodeWithTag(CARD_TAG).fetchSemanticsNode().boundsInRoot
		val link = onNodeWithText("Ver en Informe").fetchSemanticsNode().boundsInRoot

		assertTrue(abs(card.right - link.right) < 1f, "the link ends at ${link.right}, the card at ${card.right}")
	}

	private companion object {
		const val CARD_TAG = "card_link_card"
		const val CARD_WIDTH_DP = 300
	}
}
