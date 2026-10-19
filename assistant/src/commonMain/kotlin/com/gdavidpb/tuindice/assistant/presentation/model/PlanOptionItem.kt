package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One candidate plan: its label, its load and credits, and the subjects it unlocks, when it unlocks any.
data class PlanOptionItem(
	val label: String,
	val summaryText: UiText,
	val unlocksText: UiText?
)
