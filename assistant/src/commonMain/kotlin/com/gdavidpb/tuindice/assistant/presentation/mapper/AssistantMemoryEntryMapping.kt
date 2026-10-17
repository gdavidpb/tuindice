package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.MemoryEntryItem
import com.gdavidpb.tuindice.chatcontract.memory.MemoryItem

// The rows the memory screen lists: the memories still in force, in the order the server sends them.
internal suspend fun List<MemoryItem>.toMemoryEntryItems(texts: AssistantMemoryTexts): List<MemoryEntryItem> {
	return filter { item -> item.state in AssistantMemoryVocabulary.VISIBLE_STATES }
		.map { item ->
			MemoryEntryItem(
				id = item.id,
				text = item.text,
				categoryLabel = texts.categoryLabel(AssistantMemoryCategoryKey.fromWire(item.category)),
				anchorLabel = item.anchorLabel,
				needsConfirmation = item.state == AssistantMemoryVocabulary.STATE_PENDING_CONFIRMATION
			)
		}
}
