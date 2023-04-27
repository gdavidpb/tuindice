package com.gdavidpb.tuindice.record.presentation.model

data class RecordViewState(
	val quarters: List<QuarterItem>,
	val isEmpty: Boolean
)