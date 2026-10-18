package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.mapper.toAssistantConversation
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport
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

	override fun attachConversation(conversationId: String) {
		store.attachConversation(conversationId)
	}

	override fun applyEvent(event: AssistantEvent) {
		store.apply(event)
	}

	override fun markTurnUnconfirmed(turnId: String, lastSeq: Long) {
		store.markUnconfirmed(turnId, lastSeq)
	}

	override fun addUserMessage(message: AssistantUserMessage) {
		store.addUserMessage(message)
	}

	override fun addAssistantMessage(message: AssistantMessage, intent: Intent?) {
		store.addAssistantMessage(message, intent)
	}

	override fun markIntentResolved(intentId: String, outcome: OutcomeKind) {
		store.markIntentResolved(intentId, outcome)
	}

	override fun stageOutcome(report: IntentOutcomeReport) {
		store.stageOutcome(report)
	}

	override fun clearOutcome(intentId: String) {
		store.clearOutcome(intentId)
	}

	override fun pendingOutcomes(): List<IntentOutcomeReport> = store.pendingOutcomes()

	override fun markDecisionUncertain(intentId: String) {
		store.markDecisionUncertain(intentId)
	}

	override fun clearDecisionUncertain(intentId: String) {
		store.clearDecisionUncertain(intentId)
	}

	override fun isDecisionUncertain(intentId: String): Boolean = store.isDecisionUncertain(intentId)

	override suspend fun clearConversation() {
		store.clearSessionMemory()
	}
}
