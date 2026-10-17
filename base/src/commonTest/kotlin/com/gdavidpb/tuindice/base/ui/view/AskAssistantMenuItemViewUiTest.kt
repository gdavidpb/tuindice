package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class AskAssistantMenuItemViewUiTest {
	@Test
	fun when_onAskIsNull_then_theItemIsNotDrawn() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			DropdownMenu(expanded = true, onDismissRequest = {}) {
				// A sibling item shows that the menu is open, so the missing item is missing for its own reason.
				DropdownMenuItem(
					text = { Text(OtherItemTag) },
					onClick = {},
					modifier = Modifier.testTag(OtherItemTag)
				)
				AskAssistantMenuItemView(onAsk = null)
			}
		}

		assertNodeVisible(OtherItemTag)
		assertNodeHidden(AskAssistantTag)
	}

	@Test
	fun when_onAskIsProvided_then_tappingTheItemInvokesIt() = runTuIndiceUiTest {
		var askCount = 0

		setTuIndiceTestContent {
			DropdownMenu(expanded = true, onDismissRequest = {}) {
				AskAssistantMenuItemView(onAsk = { askCount++ })
			}
		}

		assertNodeVisible(AskAssistantTag)
		onNodeWithTag(AskAssistantTag).performClick()

		assertEquals(1, askCount)
	}

	private companion object {
		const val OtherItemTag = "ask_assistant_test_other_item"
		val AskAssistantTag = BaseUiTags.askAssistant("menu_item")
	}
}
