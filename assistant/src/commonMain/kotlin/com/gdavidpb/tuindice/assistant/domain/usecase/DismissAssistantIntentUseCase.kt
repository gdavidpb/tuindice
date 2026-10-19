package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantIntentRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.chatcontract.intent.IntentDecision
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** The user turns an intent down: the server is told, the intent closes as cancelled, and no outcome is reported. */
class DismissAssistantIntentUseCase(
	private val intentRepository: AssistantIntentRepository,
	private val conversationRepository: AssistantConversationRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<String, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: String): Flow<Unit> = flow {
		val sessionMark = conversationRepository.sessionMark()

		intentRepository.decide(IntentDecision(intentId = params, accepted = false))
		conversationRepository.markIntentResolved(
			intentId = params,
			outcome = OutcomeKind.CANCELLED,
			sessionMark = sessionMark
		)

		emit(Unit)
	}
}
