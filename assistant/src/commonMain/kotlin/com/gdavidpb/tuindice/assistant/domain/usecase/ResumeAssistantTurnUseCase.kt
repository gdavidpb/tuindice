package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnPhase
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Follows again the turn that was running when the conversation was last seen, from the seq it had reached.
 * Nothing is emitted when no turn runs.
 */
class ResumeAssistantTurnUseCase(
	private val turnStream: AssistantTurnStream,
	private val conversationRepository: AssistantConversationRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, AssistantEvent, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<AssistantEvent> {
		val running = conversationRepository.currentConversation().activeTurn
			?.takeIf { turn -> turn.status == AssistantTurnPhase.RUNNING }
			?: return emptyFlow()

		return turnStream.follow(turnId = running.turnId, afterSeq = running.lastSeq)
	}
}
