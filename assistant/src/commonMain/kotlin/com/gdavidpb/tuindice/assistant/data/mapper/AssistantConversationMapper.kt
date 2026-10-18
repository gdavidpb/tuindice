package com.gdavidpb.tuindice.assistant.data.mapper

import com.gdavidpb.tuindice.assistant.data.model.AssistantConversationState
import com.gdavidpb.tuindice.assistant.data.model.AssistantTurnState
import com.gdavidpb.tuindice.assistant.data.model.AssistantTurnStatus
import com.gdavidpb.tuindice.assistant.domain.model.AssistantActiveTurn
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnPhase

internal fun AssistantConversationState.toAssistantConversation(): AssistantConversation {
	return AssistantConversation(
		conversationId = conversationId,
		items = conversationItems(),
		openIntents = openIntents.mapValues { (_, open) -> open.intent },
		resolvedIntents = resolvedIntents,
		activeTurn = lastTurnId?.let { turnId -> turns[turnId] }?.toAssistantActiveTurn()
	)
}

// Messages of the user and of the assistant in the order they happened. On a tie the user's message goes first.
private fun AssistantConversationState.conversationItems(): List<AssistantConversationItem> {
	val items = userMessages.map { message -> AssistantConversationItem.User(message) } +
		messages.map { message ->
			AssistantConversationItem.Agent(message = message, turnId = messageTurnIds[message.id].orEmpty())
		}

	return items.sortedWith(compareBy({ it.createdAtMillis() }, { it is AssistantConversationItem.Agent }))
}

private fun AssistantConversationItem.createdAtMillis(): Long {
	return when (this) {
		is AssistantConversationItem.User -> message.createdAtMillis
		is AssistantConversationItem.Agent -> message.createdAtMillis
	}
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
