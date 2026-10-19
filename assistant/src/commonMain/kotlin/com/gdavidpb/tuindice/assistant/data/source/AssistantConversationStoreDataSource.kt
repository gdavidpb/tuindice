package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.mapper.toAssistantConversation
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// The domain's view of the in-memory conversation: the store keeps the data state, this maps it to the domain model.
class AssistantConversationStoreDataSource(
	private val store: AssistantConversationDataSource
) : AssistantConversationRepository {
	override fun observeConversation(): Flow<AssistantConversation> {
		return store.observeConversation().map { state -> state.toAssistantConversation() }
	}

	override fun currentConversation(): AssistantConversation {
		return store.observeConversation().value.toAssistantConversation()
	}

	override fun sessionMark(): Int = store.sessionMark()

	override fun attachConversation(conversationId: String, sessionMark: Int) {
		store.attachConversation(conversationId, sessionMark)
	}

	override fun applyEvent(event: AssistantEvent, sessionMark: Int) {
		store.apply(event, sessionMark)
	}

	override fun markTurnUnconfirmed(turnId: String, lastSeq: Long, sessionMark: Int) {
		store.markUnconfirmed(turnId, lastSeq, sessionMark)
	}

	override fun addUserMessage(message: AssistantUserMessage) {
		store.addUserMessage(message)
	}

	override fun addAssistantMessage(message: AssistantMessage, intent: Intent?, sessionMark: Int) {
		store.addAssistantMessage(message, intent, sessionMark)
	}

	override fun markIntentResolved(intentId: String, outcome: OutcomeKind, sessionMark: Int) {
		store.markIntentResolved(intentId, outcome, sessionMark)
	}

	override suspend fun clearConversation() {
		store.clearSessionMemory()
	}
}
