package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.testing.agentMessage
import com.gdavidpb.tuindice.assistant.presentation.testing.answerStillAppliesAction
import com.gdavidpb.tuindice.assistant.presentation.testing.appealAction
import com.gdavidpb.tuindice.assistant.presentation.testing.assistantConversation
import com.gdavidpb.tuindice.assistant.presentation.testing.chatTest
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.copyMessageAction
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.assistant.presentation.testing.openComponentSheetAction
import com.gdavidpb.tuindice.assistant.presentation.testing.openNavTargetAction
import com.gdavidpb.tuindice.assistant.presentation.testing.rateMessageAction
import com.gdavidpb.tuindice.assistant.presentation.testing.reportMessageAction
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.chatcontract.feedback.MessageFeedback
import com.gdavidpb.tuindice.chatcontract.feedback.ReportReason
import com.gdavidpb.tuindice.chatcontract.message.Text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssistantChatMessageBehaviorTest {
	@Test
	fun copy_handsTheTextOfTheMessageToTheDevice() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(agentMessage("a1", Text("Tu promedio es 15,4")))
		)
		viewModel.openAction(null)

		viewModel.copyMessageAction("a1")

		val copied = fixture.effects.single() as AssistantChat.Effect.CopyText
		assertEquals("Tu promedio es 15,4", copied.text)
	}

	@Test
	fun rate_recordsTheRating_andThanksTheStudent() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(agentMessage("a1", Text("hola"))))
		viewModel.openAction(null)

		viewModel.rateMessageAction("a1", useful = true)

		assertEquals(listOf(MessageFeedback(messageId = "a1", useful = true)), fixture.feedback.rated)
		assertEquals(true, (viewModel.content().items.single() as ChatItem.Agent).useful)
		val snack = fixture.effects.single() as AssistantChat.Effect.ShowSnackBar
		assertEquals("rated", snack.message)
	}

	@Test
	fun report_sendsTheReason_andConfirmsIt() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(agentMessage("a1", Text("hola"))))
		viewModel.openAction(null)

		viewModel.reportMessageAction("a1", ReportReason.INCORRECT, comment = "  no es así ")

		val report = fixture.feedback.reported.single()
		assertEquals(ReportReason.INCORRECT, report.reason)
		assertEquals("no es así", report.comment)
		val snack = fixture.effects.single() as AssistantChat.Effect.ShowSnackBar
		assertEquals("reported", snack.message)
	}

	@Test
	fun navigate_opensTheScreen_andClosesTheSheet() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)
		val refs = listOf(EntityRef(kind = EntityKind.TERM, id = "2026-2"))

		viewModel.openNavTargetAction(NavTarget.SUMMARY, refs)

		val navigation = fixture.effects.first() as AssistantChat.Effect.NavigateTo
		assertEquals(NavTarget.SUMMARY, navigation.target)
		assertEquals(refs, navigation.entityRefs)
		assertEquals(listOf<AssistantChat.Effect>(AssistantChat.Effect.Dismiss), fixture.effects.drop(1))
	}

	@Test
	fun componentSheet_opensTheComponentOnItsOwnSheet() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)

		viewModel.openComponentSheetAction("a1", "c1")

		val sheet = fixture.effects.single() as AssistantChat.Effect.OpenComponentSheet
		assertEquals("a1", sheet.messageId)
		assertEquals("c1", sheet.componentId)
	}

	@Test
	fun dismiss_closesTheSheet() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)

		viewModel.dispatch(AssistantChat.Action.Dismiss)

		assertEquals(listOf<AssistantChat.Effect>(AssistantChat.Effect.Dismiss), fixture.effects)
	}

	@Test
	fun appeal_sendsTheText_andConfirmsIt() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)

		viewModel.appealAction("  Fue un error  ")

		assertEquals(listOf("Fue un error"), fixture.standing.appeals.map { appeal -> appeal.text })
		val snack = fixture.effects.single() as AssistantChat.Effect.ShowSnackBar
		assertEquals("appeal-sent", snack.message)
	}

	@Test
	fun stillApplies_isAnsweredForTheMemory() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)

		viewModel.answerStillAppliesAction("mem1", applies = false)

		assertEquals(listOf("mem1" to false), fixture.memories.answers)
		assertTrue(fixture.effects.isEmpty())
	}
}
