package com.gdavidpb.tuindice.summary.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// What the Failed state says, already resolved to text.
data class SummaryFailedItem(
	val kind: SummaryFailedKind,
	val title: UiText,
	val message: UiText
)
