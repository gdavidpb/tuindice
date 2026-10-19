package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.model.AssistantConversationState
import com.gdavidpb.tuindice.assistant.data.model.markUnconfirmed
import com.gdavidpb.tuindice.assistant.data.model.reduce
import com.gdavidpb.tuindice.assistant.data.model.withAssistantMessage
import com.gdavidpb.tuindice.assistant.data.model.withId
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.intent.Intent
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

	// The session the caller is in, read before its request or stream starts. The writes that answer it carry this mark,
	// and a write whose session has ended since leaves the conversation as it is.
	fun sessionMark(): Int = conversation.value.sessionGeneration

	// The conversation id comes with the accepted turn, not with an event, so the caller sets it here.
	fun attachConversation(conversationId: String, sessionMark: Int) {
		conversation.update { current ->
			if (current.sessionGeneration != sessionMark) {
				current
			} else {
				current.copy(conversationId = conversationId)
			}
		}
	}

	// Idempotent by seq: an event at or below the last seq applied for its turn leaves the state as it is.
	fun apply(event: AssistantEvent, sessionMark: Int) {
		conversation.update { current ->
			if (current.sessionGeneration != sessionMark) current else current.reduce(event)
		}
	}

	// The result of a turn is unknown: its stream ended and a check from its last seq reached no final event. An
	// event that comes later shows the turn is still going.
	fun markUnconfirmed(turnId: String, lastSeq: Long, sessionMark: Int) {
		conversation.update { current ->
			if (current.sessionGeneration != sessionMark) current else current.markUnconfirmed(turnId, lastSeq)
		}
	}

	// Idempotent by id: a message that comes again is not written twice.
	fun addUserMessage(message: AssistantUserMessage) {
		conversation.update { current ->
			if (current.userMessages.any { it.id == message.id }) {
				current
			} else {
				current.copy(userMessages = current.userMessages + message, order = current.order.withId(message.id))
			}
		}
	}

	// A message no turn wrote, such as a photo's proposal. It joins the messages once, by id, and its intent, when it has
	// one, opens the way a turn's does.
	fun addAssistantMessage(message: AssistantMessage, intent: Intent?, sessionMark: Int) {
		conversation.update { current ->
			if (current.sessionGeneration != sessionMark) current else current.withAssistantMessage(message, intent)
		}
	}

	// The intent leaves the open ones and keeps the outcome it ended with.
	fun markIntentResolved(intentId: String, outcome: OutcomeKind, sessionMark: Int) {
		conversation.update { current ->
			if (current.sessionGeneration != sessionMark) {
				current
			} else {
				current.copy(
					openIntents = current.openIntents - intentId,
					resolvedIntents = current.resolvedIntents + (intentId to outcome)
				)
			}
		}
	}

	// The one write seam of the conversation for the state holders that live in it (the outbox of the intents): the
	// change is one step, so a holder's check on the session and its write cannot be split by the end of a session.
	internal fun updateConversation(transform: (AssistantConversationState) -> AssistantConversationState) {
		conversation.update(transform)
	}

	// The conversation goes, and the session it belonged to is over: its generation moves on in the same write.
	override suspend fun clearSessionMemory() {
		conversation.update { current -> AssistantConversationState(sessionGeneration = current.sessionGeneration + 1) }
	}
}
