package com.gdavidpb.tuindice.assistant.ui.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseReasonCode
import com.gdavidpb.tuindice.chatcontract.component.ComponentDisplay
import com.gdavidpb.tuindice.chatcontract.component.UnknownChatComponent
import com.gdavidpb.tuindice.chatcontract.component.WarningComponent
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test

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
	fun when_componentIsShownAsSheet_then_itsOwnSheetOffersNoDetailRow() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantComponentSheet(
				messageId = MESSAGE_ID,
				component = WarningComponent(
					id = COMPONENT_ID,
					fallbackText = FALLBACK_TEXT,
					display = ComponentDisplay.SHEET,
					reason = AbuseReasonCode.SPAM
				),
				onAction = {},
				onDismissRequest = {}
			)
		}

		onNodeWithTag(AssistantUiTags.Component.root(COMPONENT_ID)).assertIsDisplayed()
		onNodeWithTag(AssistantUiTags.Component.detail(MESSAGE_ID, COMPONENT_ID)).assertDoesNotExist()
	}

	private companion object {
		const val MESSAGE_ID = "message-1"
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Tu semestre va bien encaminado."
	}
}
