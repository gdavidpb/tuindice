package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.presentation.model.MemoryEntryItem

/**
 * Internal machine inputs of the memory screen: the outcomes of its async commands. Each outcome is its own symbol, so
 * every row keeps a fixed target, and the text the outcome shows travels with it.
 */
sealed interface AssistantMemoryInternalEvent {
	// The outcomes of a load, taken from Loading: entries to list, none to list, or the failure.
	data class EntriesLoaded(val entries: List<MemoryEntryItem>) : AssistantMemoryInternalEvent

	data object NothingLoaded : AssistantMemoryInternalEvent

	data class LoadFailed(val message: String) : AssistantMemoryInternalEvent

	// The outcomes of a delete, taken from Content: the row leaves the list, or it was the last row and the list is
	// empty.
	data class EntryDeleted(val memoryId: String, val message: String) : AssistantMemoryInternalEvent

	data class LastEntryDeleted(val memoryId: String, val message: String) : AssistantMemoryInternalEvent

	data class EntryDeleteFailed(val message: String) : AssistantMemoryInternalEvent

	// The outcomes of an answer: it was saved and the list is loaded again, or it was refused.
	data object StillAppliesAnswered : AssistantMemoryInternalEvent

	data class StillAppliesFailed(val message: String) : AssistantMemoryInternalEvent

	// The outcomes of clearing everything: the list is empty, or the clearing failed.
	data class MemoryCleared(val message: String) : AssistantMemoryInternalEvent

	data class ClearFailed(val message: String) : AssistantMemoryInternalEvent
}
