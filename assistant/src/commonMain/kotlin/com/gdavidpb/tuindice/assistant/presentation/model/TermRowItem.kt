package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One subject of a term: its grade, or when it has none, the chip of its result, or else a dash; and the chip of its
// badge, when it has one.
data class TermRowItem(
	val code: String,
	val name: String,
	val valueText: UiText?,
	val outcomeChip: CardChipItem?,
	val badgeChip: CardChipItem?
)
