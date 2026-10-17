package com.gdavidpb.tuindice.assistant.presentation.model

// One thing the assistant remembers. [needsConfirmation] shows «¿Sigue aplicando?» with its two answers.
data class MemoryEntryItem(
	val id: String,
	val text: String,
	val categoryLabel: String,
	val anchorLabel: String?,
	val needsConfirmation: Boolean
)
