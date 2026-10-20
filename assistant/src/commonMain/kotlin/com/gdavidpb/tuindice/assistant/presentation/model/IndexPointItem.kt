package com.gdavidpb.tuindice.assistant.presentation.model

// One term of the series: its index and its cumulative index, and the chip when it is the best or the worst term.
data class IndexPointItem(
	val termLabel: String,
	val termIndexText: String?,
	val cumulativeIndexText: String?,
	val markChip: CardChipItem?
)
