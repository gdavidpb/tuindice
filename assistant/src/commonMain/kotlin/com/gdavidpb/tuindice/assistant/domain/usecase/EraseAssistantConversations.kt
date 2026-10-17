package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantTurnRepository

// The threads are deleted on the server first; only then is the conversation of this device forgotten too.
// The use case and the port that the About screen calls both run these steps, so they cannot drift apart.
internal suspend fun eraseAssistantConversations(
	turnRepository: AssistantTurnRepository,
	conversationRepository: AssistantConversationRepository
) {
	turnRepository.deleteConversations()
	conversationRepository.clearConversation()
}
