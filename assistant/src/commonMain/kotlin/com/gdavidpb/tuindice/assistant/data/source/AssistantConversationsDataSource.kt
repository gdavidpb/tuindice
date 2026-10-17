package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantTurnRepository
import com.gdavidpb.tuindice.assistant.domain.usecase.eraseAssistantConversations
import com.gdavidpb.tuindice.base.domain.repository.AssistantConversationsRepository

class AssistantConversationsDataSource(
	private val turnRepository: AssistantTurnRepository,
	private val conversationRepository: AssistantConversationRepository
) : AssistantConversationsRepository {
	override suspend fun deleteAssistantConversations() {
		eraseAssistantConversations(turnRepository, conversationRepository)
	}
}
