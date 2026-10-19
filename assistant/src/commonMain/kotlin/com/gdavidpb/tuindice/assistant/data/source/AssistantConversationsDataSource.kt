package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantTurnRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantConversationsRepository

class AssistantConversationsDataSource(
	private val turnRepository: AssistantTurnRepository,
	private val conversationRepository: AssistantConversationRepository
) : AssistantConversationsRepository {
	// The threads are deleted on the server first; only then is the conversation of this device forgotten too. The use
	// case in the assistant domain takes the same two steps in the same order (DeleteAssistantConversationsUseCase).
	override suspend fun deleteAssistantConversations() {
		turnRepository.deleteConversations()
		conversationRepository.clearConversation()
	}
}
