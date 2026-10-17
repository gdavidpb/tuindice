package com.gdavidpb.tuindice.assistant.presentation.mapper

// The texts of the memory screen as ready strings. The machine reads them through this port, so its rows can run on the
// host, where the compose resources cannot be read. The module binds the resource-backed implementation.
interface AssistantMemoryTexts {
	suspend fun categoryLabel(category: AssistantMemoryCategoryKey): String

	suspend fun loadFailed(): String

	suspend fun deleted(): String

	suspend fun cleared(): String

	suspend fun deleteFailed(): String

	suspend fun answerFailed(): String
}
