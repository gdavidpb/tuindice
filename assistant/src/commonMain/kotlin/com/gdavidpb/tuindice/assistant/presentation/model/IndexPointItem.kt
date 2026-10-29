package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One term of the series: its index and its cumulative index, the sentence a screen reader says for the row, and the
// chip when it is the best or the worst term.
data class IndexPointItem(
	val termLabel: String,
	val termIndexText: String?,
	val cumulativeIndexText: String?,
	val description: UiText,
	val markChip: CardChipItem?
)
