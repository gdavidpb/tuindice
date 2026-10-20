package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class TuIndiceChevronLinkUiTest {
	@Test
	fun when_aLinkIsTapped_then_itRunsItsActionAndAnnouncesWhatTheTapOpens() = runTuIndiceUiTest {
		var taps = 0
		setTuIndiceTestContent {
			TuIndiceChevronLink(
				text = "Ver materia",
				onClick = { taps++ },
				modifier = Modifier.testTag(LINK_TAG),
				onClickLabel = "Ver materia CI5311"
			)
		}
		waitForIdle()

		onNodeWithTag(LINK_TAG).assertHasClickAction()
		val onClick = onNodeWithTag(LINK_TAG).fetchSemanticsNode().config[SemanticsActions.OnClick]
		assertEquals("Ver materia CI5311", onClick.label)

		onNodeWithText("Ver materia").performClick()
		assertEquals(1, taps)
	}

	@Test
	fun when_itLeavesTheApp_then_itIsALinkWithItsTextShown() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TuIndiceChevronLink(
				text = "Ver documento oficial",
				onClick = {},
				modifier = Modifier.testTag(LINK_TAG),
				external = true
			)
		}
		waitForIdle()

		onNodeWithText("Ver documento oficial").assertIsDisplayed()
		onNodeWithTag(LINK_TAG).assertHasClickAction()
	}

	private companion object {
		const val LINK_TAG = "chevron_link"
	}
}
