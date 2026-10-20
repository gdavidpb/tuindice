package com.gdavidpb.tuindice.assistant.presentation.model

// One subject of a term: its grade, or when it has none, the chip of its result; and the chip of its badge, when it has
// one.
data class TermRowItem(
	val code: String,
	val name: String,
	val gradeText: String?,
	val outcomeChip: CardChipItem?,
	val badgeChip: CardChipItem?
)
