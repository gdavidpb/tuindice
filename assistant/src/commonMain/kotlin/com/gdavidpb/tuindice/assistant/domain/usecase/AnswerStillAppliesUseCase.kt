package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantMemoryRepository
import com.gdavidpb.tuindice.assistant.domain.usecase.param.AnswerStillAppliesParams
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AnswerStillAppliesUseCase(
	private val memoryRepository: AssistantMemoryRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<AnswerStillAppliesParams, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: AnswerStillAppliesParams): Flow<Unit> = flow {
		memoryRepository.answerStillApplies(memoryId = params.memoryId, applies = params.applies)

		emit(Unit)
	}
}
