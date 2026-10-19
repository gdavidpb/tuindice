package com.gdavidpb.tuindice.assistant.ui.dialog

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import com.gdavidpb.tuindice.assistant.presentation.mapper.toChatComponentItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseReasonCode
import com.gdavidpb.tuindice.chatcontract.component.ComponentDisplay
import com.gdavidpb.tuindice.chatcontract.component.UnknownChatComponent
import com.gdavidpb.tuindice.chatcontract.component.WarningComponent
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class AssistantComponentSheetUiTest {
	@Test
	fun when_componentIsShown_then_itsFallbackTextIsVisible() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantComponentSheet(
				messageId = MESSAGE_ID,
				item = UnknownChatComponent(id = COMPONENT_ID, fallbackText = FALLBACK_TEXT).toChatComponentItem(),
				onAction = {},
				onDismissRequest = {}
			)
		}

		onNodeWithText(FALLBACK_TEXT).assertIsDisplayed()
	}

	@Test
	fun when_componentIsShownAsSheet_then_itsOwnSheetOffersNoDetailRow() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantComponentSheet(
				messageId = MESSAGE_ID,
				item = WarningComponent(
					id = COMPONENT_ID,
					fallbackText = FALLBACK_TEXT,
					display = ComponentDisplay.SHEET,
					reason = AbuseReasonCode.SPAM
				).toChatComponentItem(),
				onAction = {},
				onDismissRequest = {}
			)
		}

		onNodeWithTag(AssistantUiTags.Component.root(COMPONENT_ID)).assertIsDisplayed()
		onNodeWithTag(AssistantUiTags.Component.detail(MESSAGE_ID, COMPONENT_ID)).assertDoesNotExist()
	}

	@Test
	fun when_theScrimAboveTheSheetIsTapped_then_theSheetAsksToClose() = runTuIndiceUiTest {
		var dismissals = 0
		setTuIndiceTestContent {
			AssistantComponentSheet(
				messageId = MESSAGE_ID,
				item = UnknownChatComponent(id = COMPONENT_ID, fallbackText = FALLBACK_TEXT).toChatComponentItem(),
				onAction = {},
				onDismissRequest = { dismissals++ }
			)
		}

		waitForIdle()
		// The sheet draws in a layer of its own, so there are two roots; a touch on the window goes to the topmost layer.
		onAllNodes(isRoot())[0].performTouchInput { click(Offset(width / 2f, SCRIM_TAP_Y)) }
		waitForIdle()

		assertEquals(1, dismissals)
	}

	private companion object {
		const val SCRIM_TAP_Y = 20f
		const val MESSAGE_ID = "message-1"
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Tu semestre va bien encaminado."
	}
}
