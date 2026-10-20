package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.assistant.presentation.mapper.AssistantMemoryCategoryKey
import com.gdavidpb.tuindice.base.presentation.model.UiText

// One thing the assistant remembers: the category that groups it on the screen, when it was saved, its anchor, and
// [needsConfirmation], which asks «¿Sigue aplicando?» with its two answers.
data class MemoryEntryItem(
	val id: String,
	val text: String,
	val category: AssistantMemoryCategoryKey,
	val sinceText: UiText,
	val anchorLabel: String?,
	val needsConfirmation: Boolean
)
