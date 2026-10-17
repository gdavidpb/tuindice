package com.gdavidpb.tuindice.assistant.presentation.model

// One row of a batch intent: [label] and [detail] as the card shows them, and whether it is still marked.
data class IntentDraftItem(
	val mutationId: String,
	val label: String,
	val detail: String,
	val selected: Boolean
)
