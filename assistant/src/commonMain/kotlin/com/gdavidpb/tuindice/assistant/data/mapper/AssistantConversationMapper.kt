package com.gdavidpb.tuindice.assistant.data.mapper

import com.gdavidpb.tuindice.assistant.data.model.AssistantConversationState
import com.gdavidpb.tuindice.assistant.data.model.AssistantTurnState
import com.gdavidpb.tuindice.assistant.data.model.AssistantTurnStatus
import com.gdavidpb.tuindice.assistant.domain.model.AssistantActiveTurn
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnPhase
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import com.gdavidpb.tuindice.chatcontract.message.UnknownMessagePart

internal fun AssistantConversationState.toAssistantConversation(): AssistantConversation {
	return AssistantConversation(
		conversationId = conversationId,
		items = conversationItems(),
		openIntents = openIntents.mapValues { (_, open) -> open.intent },
		resolvedIntents = resolvedIntents,
		resolvedDetails = resolvedDetails,
		resolvedCounts = resolvedCounts,
		unknownOutcomeIntents = unknownOutcomeIntents,
		activeTurn = lastTurnId?.let { turnId -> turns[turnId] }?.toAssistantActiveTurn(),
		sessionMark = sessionGeneration
	)
}

// Messages of the user and of the assistant in the order they happened: the causal order the state keeps, and not the
// clocks of the device and the server, which may disagree. A message the order does not name comes after them, as the
// messages were received.
private fun AssistantConversationState.conversationItems(): List<AssistantConversationItem> {
	val users = userMessages.associateBy { message -> message.id }
	val agents = messages.associateBy { message -> message.id }

	val ordered = order.mapNotNull { id ->
		users[id]?.let { message -> AssistantConversationItem.User(message) }
			?: agents[id]?.let { message -> agentItem(message) }
	}
	val unnamed = userMessages.filter { message -> message.id !in order }.map { message ->
		AssistantConversationItem.User(message)
	} + messages.filter { message -> message.id !in order }.map { message -> agentItem(message) }

	return ordered + unnamed
}

// A message with a part this build cannot show in full gets the part a message with an unknown part has, so the chat
// shows its notice. The part is added only when the message does not carry one already.
private fun AssistantConversationState.agentItem(message: AssistantMessage): AssistantConversationItem.Agent {
	val shown = if (message.id in unreadableMessages && message.parts.none { it is UnknownMessagePart }) {
		message.copy(parts = message.parts + UnknownMessagePart)
	} else {
		message
	}

	return AssistantConversationItem.Agent(message = shown, turnId = messageTurnIds[message.id].orEmpty())
}

private fun AssistantTurnState.toAssistantActiveTurn(): AssistantActiveTurn {
	return AssistantActiveTurn(
		turnId = turnId,
		lastSeq = lastSeq,
		progressLabel = progressLabel,
		status = status.toTurnPhase(),
		failure = failure
	)
}

private fun AssistantTurnStatus.toTurnPhase(): AssistantTurnPhase {
	return when (this) {
		AssistantTurnStatus.RUNNING -> AssistantTurnPhase.RUNNING
		AssistantTurnStatus.FINISHED -> AssistantTurnPhase.FINISHED
		AssistantTurnStatus.FAILED -> AssistantTurnPhase.FAILED
		AssistantTurnStatus.UNCONFIRMED -> AssistantTurnPhase.UNCONFIRMED
	}
}
