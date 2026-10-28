package com.gdavidpb.tuindice.assistant.data.model

import com.gdavidpb.tuindice.assistant.testing.assistantIntent
import com.gdavidpb.tuindice.chatcontract.event.IntentProposed
import com.gdavidpb.tuindice.chatcontract.event.MessageCompleted
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import com.gdavidpb.tuindice.chatcontract.message.IntentPart
import com.gdavidpb.tuindice.chatcontract.message.Text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AssistantConversationOwnEventsTest {

	private val proposed = assistantIntent(id = "i1")

	// The state after a proposal of [proposed] in the message m1: the card is open.
	private fun withOpenProposal(): AssistantConversationState {
		val proposal = IntentProposed(seq = 1, turnId = "t1", messageId = "m1", intent = proposed)

		return AssistantConversationState().reduce(proposal)
	}

	private fun resolvedWithUnknownOutcome(seq: Long) = AssistantStreamEvent.Own.ResolvedWithUnknownOutcome(
		seq = seq,
		turnId = "t1",
		intentId = "i1"
	)

	@Test
	fun aResolutionWithAnUnknownOutcomeClosesTheOpenProposalWithoutAnOutcome() {
		val state = withOpenProposal().reduceOwn(resolvedWithUnknownOutcome(seq = 2))

		assertFalse("i1" in state.openIntents)
		assertTrue("i1" in state.unknownOutcomeIntents)
		assertFalse("i1" in state.resolvedIntents)
	}

	@Test
	fun theMessageOfTheClosedProposalIsMarkedToShowTheNotice() {
		val state = withOpenProposal().reduceOwn(resolvedWithUnknownOutcome(seq = 2))

		assertEquals(setOf("m1"), state.unreadableMessages)
	}

	// An intent no proposal opened is still closed, and its message is the one whose intent part carries it.
	@Test
	fun theMessageThatCarriesTheIntentIsFoundWhenNoProposalOpenedIt() {
		val withIntent = AssistantMessage(id = "m2", parts = listOf(Text("x"), IntentPart(proposed)), createdAtMillis = 1L)
		val state = AssistantConversationState()
			.reduce(MessageCompleted(seq = 1, turnId = "t1", message = withIntent))
			.reduceOwn(resolvedWithUnknownOutcome(seq = 2))

		assertEquals(setOf("m2"), state.unreadableMessages)
	}

	// A resolution the student already made keeps the outcome the student gave it.
	@Test
	fun aResolutionTheStudentAlreadyMadeKeepsItsOutcome() {
		val decided = withOpenProposal().copy(
			openIntents = emptyMap(),
			resolvedIntents = mapOf("i1" to OutcomeKind.CONFIRMED)
		)

		val state = decided.reduceOwn(resolvedWithUnknownOutcome(seq = 2))

		assertEquals(mapOf("i1" to OutcomeKind.CONFIRMED), state.resolvedIntents)
		assertTrue(state.unknownOutcomeIntents.isEmpty())
	}

	// Applying the same event again, at the same seq, changes nothing: the state is what one application made.
	@Test
	fun aRepeatedOwnEventAtTheSameSeqChangesNothing() {
		val once = withOpenProposal().reduceOwn(resolvedWithUnknownOutcome(seq = 2))

		assertEquals(once, once.reduceOwn(resolvedWithUnknownOutcome(seq = 2)))
	}

	// An app's own event moves its turn to its seq, as a contract event does: a contract event below it is already in.
	@Test
	fun anOwnEventMovesItsTurnSoALowerContractEventIsNotApplied() {
		val state = withOpenProposal().reduceOwn(
			AssistantStreamEvent.Own.UnknownIntentProposal(seq = 5, turnId = "t1", messageId = "m9")
		)
		val late = AssistantMessage(id = "m4", parts = listOf(Text("late")), createdAtMillis = 1L)

		assertEquals(state, state.reduce(MessageCompleted(seq = 4, turnId = "t1", message = late)))
	}
}
