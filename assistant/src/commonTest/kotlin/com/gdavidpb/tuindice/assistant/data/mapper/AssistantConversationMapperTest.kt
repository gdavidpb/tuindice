package com.gdavidpb.tuindice.assistant.data.mapper

import com.gdavidpb.tuindice.assistant.data.model.AssistantConversationState
import com.gdavidpb.tuindice.assistant.data.model.AssistantStreamEvent
import com.gdavidpb.tuindice.assistant.data.model.reduce
import com.gdavidpb.tuindice.assistant.data.model.reduceOwn
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.testing.assistantIntent
import com.gdavidpb.tuindice.chatcontract.event.MessageCompleted
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import com.gdavidpb.tuindice.chatcontract.message.IntentPart
import com.gdavidpb.tuindice.chatcontract.message.MessagePart
import com.gdavidpb.tuindice.chatcontract.message.Text
import com.gdavidpb.tuindice.chatcontract.message.UnknownMessagePart
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssistantConversationMapperTest {

	private val message = AssistantMessage(id = "m1", parts = listOf(Text("Hola")), createdAtMillis = 1L)

	// A proposal this build cannot read gives its message the part of an unknown part, whether the proposal arrives before
	// the message or after it. The message gets that part once.
	@Test
	fun aMessageWhoseProposalIsUnreadableGetsTheUnknownPartEitherWay() {
		val proposal = AssistantStreamEvent.Own.UnknownIntentProposal(seq = 1, turnId = "t1", messageId = "m1")
		val proposalFirst = AssistantConversationState()
			.reduceOwn(proposal)
			.reduce(MessageCompleted(seq = 2, turnId = "t1", message = message))
		val messageFirst = AssistantConversationState()
			.reduce(MessageCompleted(seq = 1, turnId = "t1", message = message))
			.reduceOwn(proposal.copy(seq = 2))

		assertEquals(listOf(Text("Hola"), UnknownMessagePart), shownParts(proposalFirst))
		assertEquals(listOf(Text("Hola"), UnknownMessagePart), shownParts(messageFirst))
	}

	// A resolution whose outcome this build does not know closes its card, and its message shows the notice once. The card
	// itself carries no outcome: the conversation only records that the intent closed without one.
	@Test
	fun aResolutionWithAnUnknownOutcomeClosesItsCardAndShowsTheNoticeOnce() {
		val intent = assistantIntent(id = "i1")
		val withIntent = AssistantMessage(
			id = "m1",
			parts = listOf(Text("Hola"), IntentPart(intent)),
			createdAtMillis = 1L
		)
		val state = AssistantConversationState()
			.reduce(MessageCompleted(seq = 1, turnId = "t1", message = withIntent))
			.reduceOwn(AssistantStreamEvent.Own.ResolvedWithUnknownOutcome(seq = 2, turnId = "t1", intentId = "i1"))

		val conversation = state.toAssistantConversation()

		assertEquals(setOf("i1"), conversation.unknownOutcomeIntents)
		assertTrue(conversation.resolvedIntents.isEmpty())
		assertEquals(listOf(Text("Hola"), IntentPart(intent), UnknownMessagePart), shownParts(state))
	}

	// A message that already carries its own unknown part keeps a single notice.
	@Test
	fun aMessageThatHasItsOwnUnknownPartGetsNoSecondOne() {
		val own = AssistantMessage(id = "m1", parts = listOf(Text("Hola"), UnknownMessagePart), createdAtMillis = 1L)
		val state = AssistantConversationState()
			.reduce(MessageCompleted(seq = 1, turnId = "t1", message = own))
			.reduceOwn(AssistantStreamEvent.Own.UnknownIntentProposal(seq = 2, turnId = "t1", messageId = "m1"))

		assertEquals(listOf(Text("Hola"), UnknownMessagePart), shownParts(state))
	}

	@Test
	fun aMessageWithoutAnUnreadablePartKeepsItsParts() {
		val state = AssistantConversationState().reduce(MessageCompleted(seq = 1, turnId = "t1", message = message))

		assertEquals(listOf(Text("Hola")), shownParts(state))
	}

	private fun shownParts(state: AssistantConversationState): List<MessagePart> {
		return state.toAssistantConversation().items
			.filterIsInstance<AssistantConversationItem.Agent>()
			.single()
			.message
			.parts
	}
}
