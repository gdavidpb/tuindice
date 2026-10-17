package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantTurnRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CancelAssistantTurnUseCase(
	private val turnRepository: AssistantTurnRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<String, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: String): Flow<Unit> = flow {
		turnRepository.cancelTurn(turnId = params)

		emit(Unit)
	}
}
