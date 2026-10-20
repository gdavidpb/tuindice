package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One evaluation: its subject, its title with its date, its grade over the maximum, and the chip of its state.
data class EvaluationRowItem(
	val code: String,
	val nameText: UiText,
	val gradeText: UiText,
	val stateChip: CardChipItem?
)
