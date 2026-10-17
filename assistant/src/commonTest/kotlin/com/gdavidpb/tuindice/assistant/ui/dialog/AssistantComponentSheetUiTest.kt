package com.gdavidpb.tuindice.assistant.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseReasonCode
import com.gdavidpb.tuindice.chatcontract.component.ComponentDisplay
import com.gdavidpb.tuindice.chatcontract.component.UnknownChatComponent
import com.gdavidpb.tuindice.chatcontract.component.WarningComponent
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class AssistantComponentSheetUiTest {
	@Test
	fun when_componentIsShown_then_itsFallbackTextIsVisible() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantComponentSheet(
				messageId = MESSAGE_ID,
				component = UnknownChatComponent(id = COMPONENT_ID, fallbackText = FALLBACK_TEXT),
				onAction = {},
				onDismissRequest = {}
			)
		}

		onNodeWithText(FALLBACK_TEXT).assertIsDisplayed()
	}

	@Test
	fun when_componentIsShownAsSheet_then_itsDetailRowReportsTheAction() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantComponentSheet(
				messageId = MESSAGE_ID,
				component = WarningComponent(
					id = COMPONENT_ID,
					fallbackText = FALLBACK_TEXT,
					display = ComponentDisplay.SHEET,
					reason = AbuseReasonCode.SPAM
				),
				onAction = { action -> actions += action },
				onDismissRequest = {}
			)
		}

		assertNodeVisible(AssistantUiTags.Component.detail(MESSAGE_ID, COMPONENT_ID))
		onNodeWithTag(AssistantUiTags.Component.detail(MESSAGE_ID, COMPONENT_ID)).performClick()

		val open = assertIs<AssistantChat.Action.OpenComponentSheet>(actions.single())
		assertEquals(COMPONENT_ID, open.componentId)
	}

	private companion object {
		const val MESSAGE_ID = "message-1"
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Tu semestre va bien encaminado."
	}
}
