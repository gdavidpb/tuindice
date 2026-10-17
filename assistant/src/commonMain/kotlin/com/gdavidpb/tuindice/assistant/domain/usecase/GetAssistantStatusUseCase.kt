package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantStandingRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.chatcontract.turn.AssistantStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetAssistantStatusUseCase(
	private val standingRepository: AssistantStandingRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, AssistantStatus, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<AssistantStatus> = flow {
		emit(standingRepository.status())
	}
}
