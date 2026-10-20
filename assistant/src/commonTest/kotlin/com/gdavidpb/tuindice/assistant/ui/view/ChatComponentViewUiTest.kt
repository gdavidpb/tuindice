package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.toChatComponentItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.model.CHAT_CARD_ROW_LIMIT
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseReasonCode
import com.gdavidpb.tuindice.chatcontract.component.ComponentDisplay
import com.gdavidpb.tuindice.chatcontract.component.SubjectListComponent
import com.gdavidpb.tuindice.chatcontract.component.SubjectRow
import com.gdavidpb.tuindice.chatcontract.component.SupportComponent
import com.gdavidpb.tuindice.chatcontract.component.SupportLine
import com.gdavidpb.tuindice.chatcontract.component.UnknownChatComponent
import com.gdavidpb.tuindice.chatcontract.component.WarningComponent
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class ChatComponentViewUiTest {
	@Test
	fun when_componentIsUnknown_then_showsItsFallbackText() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ChatComponentView(
				messageId = MESSAGE_ID,
				item = UnknownChatComponent(id = "unknown-1", fallbackText = FALLBACK_TEXT).toChatComponentItem(nowMillis = 0L),
				onAction = {}
			)
		}

		onNodeWithText(FALLBACK_TEXT).assertIsDisplayed()
	}

	@Test
	fun when_componentIsShownAsSheet_then_itsDetailRowOpensTheSheet() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			ChatComponentView(
				messageId = MESSAGE_ID,
				item = WarningComponent(
					id = COMPONENT_ID,
					fallbackText = FALLBACK_TEXT,
					display = ComponentDisplay.SHEET,
					reason = AbuseReasonCode.SPAM
				).toChatComponentItem(nowMillis = 0L),
				onAction = { actions.add(it) }
			)
		}

		assertNodeVisible(AssistantUiTags.Component.detail(MESSAGE_ID, COMPONENT_ID))
		onNodeWithTag(AssistantUiTags.Component.detail(MESSAGE_ID, COMPONENT_ID)).performClick()

		val open = assertIs<AssistantChat.Action.OpenComponentSheet>(actions.single())
		assertEquals(COMPONENT_ID, open.componentId)
	}

	@Test
	fun when_aListHasMoreRowsThanTheLimit_then_itDrawsTheLimitAndItsLinkTakesThePlaceOfTheDetailRow() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			ChatComponentView(
				messageId = MESSAGE_ID,
				item = subjects(count = 6).toChatComponentItem(nowMillis = 0L),
				onAction = { actions.add(it) }
			)
		}

		onAllNodesWithText("Ver materia").assertCountEquals(CHAT_CARD_ROW_LIMIT)
		onNodeWithText("IS-106").assertDoesNotExist()
		onNodeWithTag(AssistantUiTags.Component.detail(MESSAGE_ID, COMPONENT_ID)).assertDoesNotExist()
		onNodeWithTag(AssistantUiTags.Component.seeAll(MESSAGE_ID, COMPONENT_ID))
			.assertIsDisplayed()
			.performClick()

		val open = assertIs<AssistantChat.Action.OpenComponentSheet>(actions.single())
		assertEquals(MESSAGE_ID, open.messageId)
		assertEquals(COMPONENT_ID, open.componentId)
	}

	@Test
	fun when_aListFitsTheLimit_then_itsDetailRowStaysAndNoLinkSaysHowManyRowsThereAre() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			ChatComponentView(
				messageId = MESSAGE_ID,
				item = subjects(count = CHAT_CARD_ROW_LIMIT, display = ComponentDisplay.SHEET).toChatComponentItem(nowMillis = 0L),
				onAction = { actions.add(it) }
			)
		}

		onNodeWithTag(AssistantUiTags.Component.seeAll(MESSAGE_ID, COMPONENT_ID)).assertDoesNotExist()
		onNodeWithTag(AssistantUiTags.Component.detail(MESSAGE_ID, COMPONENT_ID))
			.assertIsDisplayed()
			.performClick()

		assertIs<AssistantChat.Action.OpenComponentSheet>(actions.single())
	}

	@Test
	fun when_supportIsShown_then_eachLineShowsItsContact() = runTuIndiceUiTest {
		val openedUris = mutableListOf<String>()

		setTuIndiceTestContent {
			CompositionLocalProvider(
				LocalUriHandler provides object : UriHandler {
					override fun openUri(uri: String) {
						openedUris += uri
					}
				}
			) {
				ChatComponentView(
					messageId = MESSAGE_ID,
					item = SupportComponent(
						id = COMPONENT_ID,
						fallbackText = FALLBACK_TEXT,
						lines = listOf(
							SupportLine(label = SUPPORT_LABEL, phone = SUPPORT_PHONE),
							SupportLine(label = SUPPORT_LABEL, url = SUPPORT_URL)
						)
					).toChatComponentItem(nowMillis = 0L),
					onAction = {}
				)
			}
		}

		onNodeWithText("Llamar al $SUPPORT_PHONE").assertIsDisplayed().performClick()
		onNodeWithText("Ver la página").assertIsDisplayed().performClick()

		assertEquals(listOf("tel:+582122222222", SUPPORT_URL), openedUris)
	}

	private fun subjects(count: Int, display: ComponentDisplay = ComponentDisplay.INLINE): SubjectListComponent =
		SubjectListComponent(
			id = COMPONENT_ID,
			fallbackText = FALLBACK_TEXT,
			display = display,
			items = (1..count).map { number -> SubjectRow("IS-10$number", "Materia $number", credits = 4) }
		)

	private companion object {
		const val MESSAGE_ID = "message-1"
		const val COMPONENT_ID = "component-1"
		const val FALLBACK_TEXT = "Revisa la pantalla de tu expediente"
		const val SUPPORT_LABEL = "Orientación"
		const val SUPPORT_PHONE = "+58 212 222 2222"
		const val SUPPORT_URL = "https://example.com/apoyo"
	}
}
