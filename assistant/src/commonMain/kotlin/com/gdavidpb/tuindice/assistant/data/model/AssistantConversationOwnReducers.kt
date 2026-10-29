package com.gdavidpb.tuindice.assistant.data.model

import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import com.gdavidpb.tuindice.chatcontract.message.IntentPart

// The app's own events follow the seq of their turn as the contract's events do: one at or below the seq the turn has
// applied is already in the state. Neither kind is final, so neither ends a run.
internal fun AssistantConversationState.reduceOwn(event: AssistantStreamEvent.Own): AssistantConversationState {
	if (turns[event.turnId].alreadyApplied(event.seq)) return this

	val seen = advancedBy(event.turnId, event.seq, isFinal = false)

	return when (event) {
		is AssistantStreamEvent.Own.UnknownIntentProposal -> seen.copy(
			unreadableMessages = seen.unreadableMessages + event.messageId
		)

		is AssistantStreamEvent.Own.ResolvedWithUnknownOutcome -> seen.resolvedWithUnknownOutcome(event.intentId)
	}
}

// A resolution whose outcome this build does not know closes its card with no outcome label, and its message shows the
// notice of a part it cannot show. A resolution the student already made keeps its outcome.
private fun AssistantConversationState.resolvedWithUnknownOutcome(intentId: String): AssistantConversationState {
	if (intentId in resolvedIntents) return this

	val messageId = openIntents[intentId]?.messageId ?: messages.messageHolding(intentId)

	return copy(
		openIntents = openIntents - intentId,
		unknownOutcomeIntents = unknownOutcomeIntents + intentId,
		unreadableMessages = messageId?.let { id -> unreadableMessages + id } ?: unreadableMessages
	)
}

// The message whose intent part carries the intent, when a message carries it.
private fun List<AssistantMessage>.messageHolding(intentId: String): String? {
	return firstOrNull { message -> message.parts.any { part -> part is IntentPart && part.intent.id == intentId } }?.id
}

// A turn that has applied an event at or above its seq already holds that event, so the event is not applied twice.
// The contract's events and the app's own events share this check.
internal fun AssistantTurnState?.alreadyApplied(seq: Long): Boolean = this != null && seq <= lastSeq

// Every event moves its turn to its seq. A final event ends the run; any other event resumes an unconfirmed turn.
internal fun AssistantConversationState.advancedBy(
	turnId: String,
	seq: Long,
	isFinal: Boolean
): AssistantConversationState {
	val current = (turns[turnId] ?: AssistantTurnState(turnId, seq)).resumedBy(isFinal)

	return copy(turns = turns + (turnId to current.copy(lastSeq = seq)), lastTurnId = turnId)
}
