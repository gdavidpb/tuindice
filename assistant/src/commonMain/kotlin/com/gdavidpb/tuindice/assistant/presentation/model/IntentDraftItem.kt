package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One row of a batch intent: [label] and [detail] as the card shows them, and whether it is still marked.
data class IntentDraftItem(
	val mutationId: String,
	val label: String,
	val detail: UiText,
	val selected: Boolean
)
