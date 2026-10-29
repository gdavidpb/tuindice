package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow

/** The conversation as it stands in memory. It reads the local store only; the other use cases ask the server. */
class ObserveAssistantConversationUseCase(
	private val conversationRepository: AssistantConversationRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, AssistantConversation, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<AssistantConversation> {
		return conversationRepository.observeConversation()
	}
}
