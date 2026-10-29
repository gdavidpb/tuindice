package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantMemoryRepository
import com.gdavidpb.tuindice.chatcontract.memory.MemoryList
import com.gdavidpb.tuindice.chatcontract.memory.StillAppliesAnswer

// Nothing is kept here: every call asks the server, so there is no session state to let go of when the session ends.
class AssistantMemoryDataSource(
	private val api: KtorAssistantApiDataSource
) : AssistantMemoryRepository {

	override suspend fun listMemories(): MemoryList = api.getMemories()

	override suspend fun deleteMemory(memoryId: String) {
		api.deleteMemory(memoryId)
	}

	override suspend fun deleteAllMemories() {
		api.deleteAllMemories()
	}

	override suspend fun answerStillApplies(memoryId: String, applies: Boolean) {
		api.postStillApplies(memoryId, StillAppliesAnswer(applies = applies))
	}
}
