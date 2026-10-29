package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantMemoryRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteAssistantMemoryItemUseCase(
	private val memoryRepository: AssistantMemoryRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<String, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: String): Flow<Unit> = flow {
		memoryRepository.deleteMemory(memoryId = params)

		emit(Unit)
	}
}
