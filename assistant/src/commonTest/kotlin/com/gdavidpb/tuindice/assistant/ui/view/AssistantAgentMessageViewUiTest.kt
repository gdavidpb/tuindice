package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.agentMessage
import com.gdavidpb.tuindice.chatcontract.feedback.ReportReason
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AssistantAgentMessageViewUiTest {
	@Test
	fun when_copyAndThumbsAreTapped_then_eachDispatchesItsAction() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantAgentMessageView(item = agentMessage(id = MESSAGE_ID), onAction = { actions.add(it) })
		}

		assertNodeVisible(AssistantUiTags.Message.copy(MESSAGE_ID))
		onNodeWithTag(AssistantUiTags.Message.copy(MESSAGE_ID)).performClick()
		onNodeWithTag(AssistantUiTags.Message.useful(MESSAGE_ID)).performClick()
		onNodeWithTag(AssistantUiTags.Message.notUseful(MESSAGE_ID)).performClick()

		assertIs<AssistantChat.Action.CopyMessage>(actions[0])
		assertTrue(assertIs<AssistantChat.Action.RateMessage>(actions[1]).useful)
		assertFalse(assertIs<AssistantChat.Action.RateMessage>(actions[2]).useful)
	}

	@Test
	fun when_useful_then_thatThumbIsSelected() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantAgentMessageView(item = agentMessage(id = MESSAGE_ID, useful = true), onAction = {})
		}

		assertNodeVisible(AssistantUiTags.Message.useful(MESSAGE_ID))
		onNodeWithTag(AssistantUiTags.Message.useful(MESSAGE_ID)).assertIsSelected()
		onNodeWithTag(AssistantUiTags.Message.notUseful(MESSAGE_ID)).assertIsNotSelected()
	}

	@Test
	fun when_reportReasonIsChosen_then_reportsTheMessageWithoutComment() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			AssistantAgentMessageView(item = agentMessage(id = MESSAGE_ID), onAction = { actions.add(it) })
		}

		onNodeWithTag(AssistantUiTags.Message.report(MESSAGE_ID)).performClick()
		assertNodeVisible(AssistantUiTags.Message.reportReason(MESSAGE_ID, ReportReason.HARMFUL))
		onNodeWithTag(AssistantUiTags.Message.reportReason(MESSAGE_ID, ReportReason.HARMFUL)).performClick()

		val report = assertIs<AssistantChat.Action.ReportMessage>(actions.single())
		assertEquals(ReportReason.HARMFUL, report.reason)
		assertNull(report.comment)
	}

	private companion object {
		const val MESSAGE_ID = "message-1"
	}
}
