package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.usecase.param.FollowAssistantTurnParams
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import kotlinx.coroutines.flow.Flow

/** Follows a turn from its afterSeq on. Each event is applied to the conversation before it is emitted. */
class FollowAssistantTurnUseCase(
	private val turnStream: AssistantTurnStream,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<FollowAssistantTurnParams, AssistantEvent, Nothing>() {
	override suspend fun executeOnBackground(params: FollowAssistantTurnParams): Flow<AssistantEvent> {
		return turnStream.follow(turnId = params.turnId, afterSeq = params.afterSeq)
	}
}
