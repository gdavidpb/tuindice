package com.gdavidpb.tuindice.assistant.presentation.mapper

// The memory vocabulary the server sends. Its source is academiccore: MemoryState.kt and MemoryCategory.kt, in
// academiccore/src/commonMain/kotlin/com/gdavidpb/tuindice/academiccore/domain/model/memory. This module does not
// depend on academiccore, so the names are copied here and must follow those enums.
object AssistantMemoryVocabulary {
	const val STATE_ACTIVE = "ACTIVE"
	const val STATE_PENDING_CONFIRMATION = "PENDING_CONFIRMATION"

	// The memories the list shows. An archived, replaced or deleted memory is not listed.
	val VISIBLE_STATES: Set<String> = setOf(STATE_ACTIVE, STATE_PENDING_CONFIRMATION)
}
