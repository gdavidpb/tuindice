package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.model.AssistantConversationState
import com.gdavidpb.tuindice.assistant.data.model.AssistantOpenIntent
import com.gdavidpb.tuindice.assistant.data.model.AssistantTurnState
import com.gdavidpb.tuindice.assistant.data.model.AssistantTurnStatus
import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnFailure
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
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
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// The active conversation, built from the events of its turns and from what the user sent. It lives in memory only.
class AssistantConversationDataSource : SessionMemory {

	private val conversation = MutableStateFlow(AssistantConversationState())

	fun observeConversation(): StateFlow<AssistantConversationState> = conversation.asStateFlow()

	// The conversation id comes with the accepted turn, not with an event, so the caller sets it here.
	fun attachConversation(conversationId: String) {
		conversation.update { current -> current.copy(conversationId = conversationId) }
	}

	// Idempotent by seq: an event at or below the last seq applied for its turn leaves the state as it is.
	fun apply(event: AssistantEvent) {
		conversation.update { current -> current.reduce(event) }
	}

	// Idempotent by id: a message that comes again is not written twice.
	fun addUserMessage(message: AssistantUserMessage) {
		conversation.update { current ->
			if (current.userMessages.any { it.id == message.id }) {
				current
			} else {
				current.copy(userMessages = current.userMessages + message)
			}
		}
	}

	// The intent leaves the open ones and keeps the outcome it ended with.
	fun markIntentResolved(intentId: String, outcome: OutcomeKind) {
		conversation.update { current ->
			current.copy(
				openIntents = current.openIntents - intentId,
				resolvedIntents = current.resolvedIntents + (intentId to outcome)
			)
		}
	}

	override suspend fun clearSessionMemory() {
		conversation.value = AssistantConversationState()
	}
}

private fun AssistantConversationState.reduce(event: AssistantEvent): AssistantConversationState {
	val known = turns[event.turnId]

	if (event is UnknownAssistantEvent || (known != null && event.seq <= known.lastSeq)) return this

	val current = known ?: AssistantTurnState(event.turnId, event.seq)
	val seen = copy(
		turns = turns + (event.turnId to current.copy(lastSeq = event.seq)),
		lastTurnId = event.turnId
	)

	return when (event) {
		is RunStarted -> seen.updateTurn(event.turnId) { turn ->
			turn.copy(progressLabel = null, status = AssistantTurnStatus.RUNNING, failure = null)
		}

		is Progress -> seen.updateTurn(event.turnId) { turn -> turn.copy(progressLabel = event.label) }

		// The components arrive complete in the message, so the stream only advances the seq for them.
		is ComponentEmitted -> seen

		is MessageCompleted -> seen.copy(
			messages = seen.messages.withMessage(event.message),
			messageTurnIds = seen.messageTurnIds + (event.message.id to event.turnId)
		)

		is IntentProposed -> {
			val open = AssistantOpenIntent(
				intentId = event.intent.id,
				turnId = event.turnId,
				messageId = event.messageId,
				intent = event.intent
			)

			seen.copy(openIntents = seen.openIntents + (open.intentId to open))
		}

		is IntentResolved -> seen.copy(
			openIntents = seen.openIntents - event.intentId,
			resolvedIntents = seen.resolvedIntents + (event.intentId to event.outcome)
		)

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

private fun AssistantConversationState.updateTurn(
	turnId: String,
	update: (AssistantTurnState) -> AssistantTurnState
): AssistantConversationState {
	return copy(turns = turns + (turnId to update(turns.getValue(turnId))))
}

// A message that comes again replaces the one it already has, so it never shows twice.
private fun List<AssistantMessage>.withMessage(message: AssistantMessage): List<AssistantMessage> {
	return if (any { it.id == message.id }) {
		map { current -> if (current.id == message.id) message else current }
	} else {
		this + message
	}
}
