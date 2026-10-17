package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class AssistantChatNoticeViewUiTest {
	@Test
	fun when_turnFailedCanRetry_then_retryDispatchesRetryTurn() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatNoticeView(
				notice = ChatNoticeItem.TurnFailed(message = FAILED_MESSAGE, canRetry = true),
				onAction = { actions.add(it) }
			)
		}

		assertNodeVisible(AssistantUiTags.NoticeRetry)
		onNodeWithTag(AssistantUiTags.NoticeRetry).performClick()

		assertIs<AssistantChat.Action.RetryTurn>(actions.single())
	}

	@Test
	fun when_restrictionCanBeAppealed_then_appealSendsTheWrittenText() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantChatNoticeView(
				notice = ChatNoticeItem.Restricted(message = RESTRICTED_MESSAGE, canAppeal = true),
				onAction = { actions.add(it) }
			)
		}

		assertNodeVisible(AssistantUiTags.AppealAction)
		onNodeWithTag(AssistantUiTags.AppealAction).performClick()
		assertNodeVisible(AssistantUiTags.AppealText)
		onNodeWithTag(AssistantUiTags.AppealText).performTextInput(APPEAL)
		onNodeWithTag(AssistantUiTags.AppealSend).performClick()

		assertEquals(APPEAL, assertIs<AssistantChat.Action.Appeal>(actions.single()).text)
	}

	@Test
	fun when_quotaIsExhausted_then_itShowsItsMessageAsANotice() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantChatNoticeView(
				notice = ChatNoticeItem.QuotaExhausted(message = QUOTA_MESSAGE),
				onAction = {}
			)
		}

		assertNodeVisible(AssistantUiTags.Notice)
		onNodeWithText(QUOTA_MESSAGE).assertIsDisplayed()
	}

	private companion object {
		const val FAILED_MESSAGE = "No pudimos responder. Intenta de nuevo."
		const val RESTRICTED_MESSAGE = "El asistente está en pausa."
		const val APPEAL = "Fue un error"
		const val QUOTA_MESSAGE = "Llegaste al límite de hoy."
	}
}
