package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.IntentOutcomeReport
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import kotlinx.coroutines.flow.Flow

/** The active conversation, built from the events of its turns and from what the user sent. It lives in memory only. */
interface AssistantConversationRepository {
	fun observeConversation(): Flow<AssistantConversation>

	fun currentConversation(): AssistantConversation

	fun attachConversation(conversationId: String)

	fun applyEvent(event: AssistantEvent)

	/**
	 * Marks the turn [turnId] as unconfirmed: its stream and a check from [lastSeq] reached no final event. A turn the
	 * conversation has seen no event of yet is named by the mark too, so the student sees where its send stands.
	 */
	fun markTurnUnconfirmed(turnId: String, lastSeq: Long)

	fun addUserMessage(message: AssistantUserMessage)

	/** Writes a message of the assistant that no turn wrote, such as a photo's proposal, with the intent it carries. */
	fun addAssistantMessage(message: AssistantMessage, intent: Intent?)

	fun markIntentResolved(intentId: String, outcome: OutcomeKind)

	/** Keeps an outcome report the server has not taken yet, until it takes it: the next flush sends it again. */
	fun stageOutcome(report: IntentOutcomeReport)

	fun clearOutcome(intentId: String)

	/** The outcome reports the server has not taken yet. */
	fun pendingOutcomes(): List<IntentOutcomeReport>

	/** Marks a decision whose answer the network lost: it may have been applied already. */
	fun markDecisionUncertain(intentId: String)

	fun clearDecisionUncertain(intentId: String)

	fun isDecisionUncertain(intentId: String): Boolean

	/** Forgets the conversation on this device, as when its threads are deleted on the server. */
	suspend fun clearConversation()
}
