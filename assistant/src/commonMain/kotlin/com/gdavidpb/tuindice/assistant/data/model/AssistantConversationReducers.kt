package com.gdavidpb.tuindice.assistant.data.model

import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnFailure
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.event.ComponentEmitted
import com.gdavidpb.tuindice.chatcontract.event.IntentProposed
import com.gdavidpb.tuindice.chatcontract.event.IntentResolved
import com.gdavidpb.tuindice.chatcontract.event.MessageCompleted
import com.gdavidpb.tuindice.chatcontract.event.Progress
import com.gdavidpb.tuindice.chatcontract.event.RunError
import com.gdavidpb.tuindice.chatcontract.event.RunFinished
import com.gdavidpb.tuindice.chatcontract.event.RunStarted
import com.gdavidpb.tuindice.chatcontract.event.UnknownAssistantEvent
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage

internal fun AssistantConversationState.reduce(event: AssistantEvent): AssistantConversationState {
	val known = turns[event.turnId]

	if (event is UnknownAssistantEvent || known.alreadyApplied(event.seq)) return this

	val seen = advancedBy(event.turnId, event.seq, isFinal = event is RunFinished || event is RunError)

	return when (event) {
		is RunStarted -> seen.updateTurn(event.turnId) { turn ->
			turn.copy(progressLabel = null, status = AssistantTurnStatus.RUNNING, failure = null)
		}

		is Progress -> seen.updateTurn(event.turnId) { turn -> turn.copy(progressLabel = event.label) }

		// The components arrive complete in the message, so the stream only advances the seq for them.
		is ComponentEmitted -> seen

		is MessageCompleted -> seen.copy(
			messages = seen.messages.withMessage(event.message),
			messageTurnIds = seen.messageTurnIds + (event.message.id to event.turnId),
			order = seen.order.withId(event.message.id)
		)

		is IntentProposed -> seen.proposedBy(event)

		is IntentResolved -> seen.resolvedBy(event)

		is RunFinished -> seen.updateTurn(event.turnId) { turn ->
			turn.copy(progressLabel = null, status = AssistantTurnStatus.FINISHED)
		}

		is RunError -> seen.updateTurn(event.turnId) { turn ->
			turn.copy(
				progressLabel = null,
				status = AssistantTurnStatus.FAILED,
				failure = AssistantTurnFailure(
					code = event.code,
					retryable = event.retryable,
					retryAfterSeconds = event.retryAfterSeconds
				)
			)
		}

		// Dropped before this point; the branch keeps the when exhaustive.
		is UnknownAssistantEvent -> this
	}
}

// A proposal of an intent the student already resolved does not open it again: the intent stays resolved.
private fun AssistantConversationState.proposedBy(event: IntentProposed): AssistantConversationState {
	val open = AssistantOpenIntent(
		intentId = event.intent.id,
		turnId = event.turnId,
		messageId = event.messageId,
		intent = event.intent
	)

	return if (event.intent.id in resolvedIntents) this else copy(openIntents = openIntents + (open.intentId to open))
}

// The server closes a proposal another one replaced. An intent the app already resolved here (the student decided it
// while the turn that replaces it ran) keeps the outcome the app gave it.
private fun AssistantConversationState.resolvedBy(event: IntentResolved): AssistantConversationState {
	val kept = event.intentId in resolvedIntents

	return copy(
		openIntents = openIntents - event.intentId,
		resolvedIntents = if (kept) resolvedIntents else resolvedIntents + (event.intentId to event.outcome)
	)
}

// A turn that has ended keeps its end. A turn with no event yet starts here, unconfirmed, at the seq it was followed
// from.
internal fun AssistantConversationState.markUnconfirmed(turnId: String, lastSeq: Long): AssistantConversationState {
	val turn = turns[turnId]

	return when {
		turn == null -> copy(
			turns = turns + (turnId to AssistantTurnState(turnId, lastSeq, status = AssistantTurnStatus.UNCONFIRMED)),
			lastTurnId = turnId
		)

		turn.status == AssistantTurnStatus.RUNNING -> updateTurn(turnId) { current ->
			current.copy(progressLabel = null, status = AssistantTurnStatus.UNCONFIRMED)
		}

		else -> this
	}
}

// An unconfirmed turn that delivers an event that is not its final one is running again.
internal fun AssistantTurnState.resumedBy(isFinal: Boolean): AssistantTurnState {
	return if (status == AssistantTurnStatus.UNCONFIRMED && !isFinal) copy(status = AssistantTurnStatus.RUNNING) else this
}

private fun AssistantConversationState.updateTurn(
	turnId: String,
	update: (AssistantTurnState) -> AssistantTurnState
): AssistantConversationState {
	return copy(turns = turns + (turnId to update(turns.getValue(turnId))))
}

// A proposal written outside a turn, as a photo's is, belongs to no turn: its open intent carries an empty turn id. An
// intent the student already resolved stays resolved: a message that comes again does not open it once more.
internal fun AssistantConversationState.withAssistantMessage(
	message: AssistantMessage,
	intent: Intent?
): AssistantConversationState {
	val open = intent
		?.takeUnless { resolvedIntent -> resolvedIntent.id in resolvedIntents }
		?.let { openIntent ->
			AssistantOpenIntent(intentId = openIntent.id, turnId = "", messageId = message.id, intent = openIntent)
		}

	return copy(
		messages = messages.withMessage(message),
		order = order.withId(message.id),
		openIntents = if (open == null) openIntents else openIntents + (open.intentId to open)
	)
}

// An id joins the order once, where it first came in; a message that comes again keeps its place.
internal fun List<String>.withId(id: String): List<String> = if (id in this) this else this + id

// A message that comes again replaces the one it already has, so it never shows twice.
private fun List<AssistantMessage>.withMessage(message: AssistantMessage): List<AssistantMessage> {
	return if (any { it.id == message.id }) {
		map { current -> if (current.id == message.id) message else current }
	} else {
		this + message
	}
}
