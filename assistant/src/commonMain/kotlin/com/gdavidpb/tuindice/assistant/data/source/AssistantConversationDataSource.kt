package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.model.AssistantConversationState
import com.gdavidpb.tuindice.assistant.data.model.AssistantOpenIntent
import com.gdavidpb.tuindice.assistant.data.model.AssistantTurnState
import com.gdavidpb.tuindice.assistant.data.model.AssistantTurnStatus
import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnFailure
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantIntentOutboxRepository
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
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// The active conversation, built from the events of its turns and from what the user sent. It lives in memory only.
class AssistantConversationDataSource : SessionMemory {

	private val conversation = MutableStateFlow(AssistantConversationState())

	// The outbox of the intents lives in the conversation state: the wipe that clears the conversation clears it too.
	val outbox: AssistantIntentOutboxRepository = ConversationOutboxDataSource(conversation)

	fun observeConversation(): StateFlow<AssistantConversationState> = conversation.asStateFlow()

	// The conversation id comes with the accepted turn, not with an event, so the caller sets it here.
	fun attachConversation(conversationId: String) {
		conversation.update { current -> current.copy(conversationId = conversationId) }
	}

	// Idempotent by seq: an event at or below the last seq applied for its turn leaves the state as it is.
	fun apply(event: AssistantEvent) {
		conversation.update { current -> current.reduce(event) }
	}

	// The result of a turn is unknown: its stream ended and a check from its last seq reached no final event. An
	// event that comes later shows the turn is still going.
	fun markUnconfirmed(turnId: String, lastSeq: Long) {
		conversation.update { current -> current.markUnconfirmed(turnId, lastSeq) }
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
	fun addAssistantMessage(message: AssistantMessage, intent: Intent?) {
		conversation.update { current -> current.withAssistantMessage(message, intent) }
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

	val current = (known ?: AssistantTurnState(event.turnId, event.seq)).resumedBy(event)
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
			messageTurnIds = seen.messageTurnIds + (event.message.id to event.turnId),
			order = seen.order.withId(event.message.id)
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

// A turn that has ended keeps its end. A turn with no event yet starts here, unconfirmed, at the seq it was followed
// from.
private fun AssistantConversationState.markUnconfirmed(turnId: String, lastSeq: Long): AssistantConversationState {
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
private fun AssistantTurnState.resumedBy(event: AssistantEvent): AssistantTurnState {
	val isFinal = event is RunFinished || event is RunError

	return if (status == AssistantTurnStatus.UNCONFIRMED && !isFinal) copy(status = AssistantTurnStatus.RUNNING) else this
}

private fun AssistantConversationState.updateTurn(
	turnId: String,
	update: (AssistantTurnState) -> AssistantTurnState
): AssistantConversationState {
	return copy(turns = turns + (turnId to update(turns.getValue(turnId))))
}

// A proposal written outside a turn, as a photo's is, belongs to no turn: its open intent carries an empty turn id.
private fun AssistantConversationState.withAssistantMessage(
	message: AssistantMessage,
	intent: Intent?
): AssistantConversationState {
	val open = intent?.let { AssistantOpenIntent(intentId = it.id, turnId = "", messageId = message.id, intent = it) }

	return copy(
		messages = messages.withMessage(message),
		order = order.withId(message.id),
		openIntents = if (open == null) openIntents else openIntents + (open.intentId to open)
	)
}

// An id joins the order once, where it first came in; a message that comes again keeps its place.
private fun List<String>.withId(id: String): List<String> = if (id in this) this else this + id

// A message that comes again replaces the one it already has, so it never shows twice.
private fun List<AssistantMessage>.withMessage(message: AssistantMessage): List<AssistantMessage> {
	return if (any { it.id == message.id }) {
		map { current -> if (current.id == message.id) message else current }
	} else {
		this + message
	}
}

// The intents' outbox over the conversation: its reports are fields of the conversation state.
private class ConversationOutboxDataSource(
	private val conversation: MutableStateFlow<AssistantConversationState>
) : AssistantIntentOutboxRepository {
	override fun stageOutcome(report: IntentOutcomeReport) {
		conversation.update { current ->
			current.copy(outcomesPending = current.outcomesPending + (report.intentId to report))
		}
	}

	override fun clearOutcome(intentId: String) {
		conversation.update { current -> current.copy(outcomesPending = current.outcomesPending - intentId) }
	}

	override fun pendingOutcomes(): List<IntentOutcomeReport> = conversation.value.outcomesPending.values.toList()
}
