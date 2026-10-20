package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.MemoryEntryItem
import com.gdavidpb.tuindice.chatcontract.memory.MemoryItem
import com.gdavidpb.tuindice.chatcontract.memory.MemoryVocabulary

// The memories the list shows: the ones still in force. An archived, replaced or deleted memory is not listed.
private val VISIBLE_STATES: Set<String> = setOf(
	MemoryVocabulary.STATE_ACTIVE,
	MemoryVocabulary.STATE_PENDING_CONFIRMATION
)

// The rows the memory screen lists: the memories still in force, in the order the server sends them. [nowMillis] is the
// moment the list is read, the one the dates of the memories are told against.
internal fun List<MemoryItem>.toMemoryEntryItems(nowMillis: Long): List<MemoryEntryItem> {
	return filter { item -> item.state in VISIBLE_STATES }
		.map { item ->
			MemoryEntryItem(
				id = item.id,
				text = item.text,
				category = AssistantMemoryCategoryKey.fromWire(item.category),
				sinceText = item.createdAtMillis.toMemorySinceText(nowMillis = nowMillis),
				anchorLabel = item.anchorLabel,
				needsConfirmation = item.state == MemoryVocabulary.STATE_PENDING_CONFIRMATION
			)
		}
}
