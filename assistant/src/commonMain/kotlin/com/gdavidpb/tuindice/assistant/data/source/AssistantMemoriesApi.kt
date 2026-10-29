package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.chatcontract.memory.MemoryList
import com.gdavidpb.tuindice.chatcontract.memory.StillAppliesAnswer
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post

internal suspend fun KtorAssistantApiDataSource.getMemories(): MemoryList {
	return send { get(assistantPath("memories")) }.decodeBody()
}

internal suspend fun KtorAssistantApiDataSource.deleteMemory(memoryId: String) {
	send { delete(assistantPath("memories", memoryId)) }
}

internal suspend fun KtorAssistantApiDataSource.deleteAllMemories() {
	send { delete(assistantPath("memories")) }
}

internal suspend fun KtorAssistantApiDataSource.postStillApplies(memoryId: String, answer: StillAppliesAnswer) {
	send { post(assistantPath("memories", memoryId, "still-applies")) { setJsonBody(answer) } }
}
