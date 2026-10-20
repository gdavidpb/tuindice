package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
