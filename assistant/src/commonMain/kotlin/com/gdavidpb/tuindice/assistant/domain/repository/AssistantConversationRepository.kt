package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import kotlinx.coroutines.flow.Flow

/** The active conversation, built from the events of its turns and from what the user sent. It lives in memory only. */
interface AssistantConversationRepository {
	fun observeConversation(): Flow<AssistantConversation>

	fun currentConversation(): AssistantConversation

	fun attachConversation(conversationId: String)

	fun applyEvent(event: AssistantEvent)

	fun addUserMessage(message: AssistantUserMessage)

	/** Writes a message of the assistant that no turn wrote, such as a photo's proposal, with the intent it carries. */
	fun addAssistantMessage(message: AssistantMessage, intent: Intent?)

	fun markIntentResolved(intentId: String, outcome: OutcomeKind)

	/** Forgets the conversation on this device, as when its threads are deleted on the server. */
	suspend fun clearConversation()
}
