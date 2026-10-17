package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.chatcontract.memory.MemoryList
import com.gdavidpb.tuindice.chatcontract.memory.StillAppliesAnswer

class AssistantMemoryDataSource(
	private val api: KtorAssistantApiDataSource
) : SessionMemory {

	suspend fun listMemories(): MemoryList = api.getMemories()

	suspend fun deleteMemory(memoryId: String) {
		api.deleteMemory(memoryId)
	}

	suspend fun deleteAllMemories() {
		api.deleteAllMemories()
	}

	suspend fun answerStillApplies(memoryId: String, applies: Boolean) {
		api.postStillApplies(memoryId, StillAppliesAnswer(applies = applies))
	}

	// Nothing is kept here: every call asks the server, so there is nothing to let go of when the session ends.
	override suspend fun clearSessionMemory() = Unit
}
