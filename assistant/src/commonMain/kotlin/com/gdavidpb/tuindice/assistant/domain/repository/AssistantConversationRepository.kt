package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.assistant.domain.model.IntentResolvedCount
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import kotlinx.coroutines.flow.Flow

/** The active conversation, built from the events of its turns and from what the user sent. It lives in memory only. */
interface AssistantConversationRepository {
	fun observeConversation(): Flow<AssistantConversation>

	fun currentConversation(): AssistantConversation

	/**
	 * The session the caller is in, read before its request or stream starts. The writes that answer that request carry
	 * it: once the session has ended, they leave the conversation as it is.
	 */
	fun sessionMark(): Int

	fun attachConversation(conversationId: String, sessionMark: Int)

	fun applyEvent(event: AssistantEvent, sessionMark: Int)

	/**
	 * Marks the turn [turnId] as unconfirmed: its stream and a check from [lastSeq] reached no final event. A turn the
	 * conversation has seen no event of yet is named by the mark too, so the student sees where its send stands.
	 */
	fun markTurnUnconfirmed(turnId: String, lastSeq: Long, sessionMark: Int)

	fun addUserMessage(message: AssistantUserMessage)

	/** Writes a message of the assistant that no turn wrote, such as a photo's proposal, with the intent it carries. */
	fun addAssistantMessage(message: AssistantMessage, intent: Intent?, sessionMark: Int)

	/** Marks the intent [intentId] resolved with [outcome]. [count] is kept with it when the intent adds evaluations. */
	fun markIntentResolved(
		intentId: String,
		outcome: OutcomeKind,
		sessionMark: Int,
		count: IntentResolvedCount? = null
	)

	/** Forgets the conversation on this device, as when its threads are deleted on the server. */
	suspend fun clearConversation()
}
