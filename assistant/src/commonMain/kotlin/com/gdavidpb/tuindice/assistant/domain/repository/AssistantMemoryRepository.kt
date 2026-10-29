package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.chatcontract.memory.MemoryList

/** What the assistant remembers about the user. It is asked of the server every time, never kept on the device. */
interface AssistantMemoryRepository {
	suspend fun listMemories(): MemoryList

	suspend fun deleteMemory(memoryId: String)

	suspend fun deleteAllMemories()

	suspend fun answerStillApplies(memoryId: String, applies: Boolean)
}
