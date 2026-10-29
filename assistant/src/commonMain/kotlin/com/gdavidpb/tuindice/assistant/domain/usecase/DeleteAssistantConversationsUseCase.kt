package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantTurnRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteAssistantConversationsUseCase(
	private val turnRepository: AssistantTurnRepository,
	private val conversationRepository: AssistantConversationRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> = flow {
		// The threads are deleted on the server first; only then is the conversation of this device forgotten too.
		turnRepository.deleteConversations()
		conversationRepository.clearConversation()

		emit(Unit)
	}
}
