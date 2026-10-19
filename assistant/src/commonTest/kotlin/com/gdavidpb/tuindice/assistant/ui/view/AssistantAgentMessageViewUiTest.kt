package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.AssistantReportReason
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.agentMessage
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
		assertNodeVisible(AssistantUiTags.Message.reportReason(MESSAGE_ID, AssistantReportReason.HARMFUL))
		onNodeWithTag(AssistantUiTags.Message.reportReason(MESSAGE_ID, AssistantReportReason.HARMFUL)).performClick()

		val report = assertIs<AssistantChat.Action.ReportMessage>(actions.single())
		assertEquals(AssistantReportReason.HARMFUL, report.reason)
		assertNull(report.comment)
	}

	@Test
	fun when_theActionsAreShown_then_eachIconIs18DpOnAButtonOf36Dp_withNoSpaceBetweenThem() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			AssistantAgentMessageView(item = agentMessage(id = MESSAGE_ID), onAction = {})
		}

		val tags = listOf(
			AssistantUiTags.Message.copy(MESSAGE_ID),
			AssistantUiTags.Message.useful(MESSAGE_ID),
			AssistantUiTags.Message.notUseful(MESSAGE_ID),
			AssistantUiTags.Message.report(MESSAGE_ID)
		)
		tags.forEach { tag ->
			onNodeWithTag(tag).assertWidthIsEqualTo(36.dp).assertHeightIsEqualTo(36.dp)
		}
		ACTION_DESCRIPTIONS.forEach { description ->
			onNodeWithContentDescription(description, useUnmergedTree = true)
				.assertWidthIsEqualTo(18.dp)
				.assertHeightIsEqualTo(18.dp)
		}
		val bounds = tags.map { tag -> onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot }
		bounds.zipWithNext().forEach { (previous, next) ->
			assertEquals(previous.right, next.left, absoluteTolerance = PIXEL_TOLERANCE)
		}
	}

	@Test
	fun when_aTouchLandsJustOutsideAnAction_then_itStillReachesIt() = runTuIndiceUiTest {
		val actions = mutableListOf<AssistantChat.Action>()
		setTuIndiceTestContent {
			Column(modifier = Modifier.padding(all = 24.dp)) {
				AssistantMessageActionsView(messageId = MESSAGE_ID, useful = null, onAction = { actions.add(it) })
			}
		}

		// 5 dp above the copy button: inside its 48 dp touch area, outside the 36 dp it draws.
		onNodeWithTag(AssistantUiTags.Message.copy(MESSAGE_ID)).performTouchInput {
			click(Offset(x = centerX, y = -5.dp.toPx()))
		}

		assertIs<AssistantChat.Action.CopyMessage>(actions.single())
	}

	private companion object {
		const val MESSAGE_ID = "message-1"
		const val PIXEL_TOLERANCE = 1f
		val ACTION_DESCRIPTIONS = listOf("Copiar", "Útil", "No útil", "Reportar")
	}
}
