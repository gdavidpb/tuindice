package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantMemoryRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.chatcontract.memory.MemoryList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Asks the server for what the assistant remembers. It is a remote read, so it is not an observation. */
class GetAssistantMemoryUseCase(
	private val memoryRepository: AssistantMemoryRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, MemoryList, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<MemoryList> = flow {
		emit(memoryRepository.listMemories())
	}
}
