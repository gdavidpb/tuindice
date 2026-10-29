package com.gdavidpb.tuindice.record.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class TermMetricDelta(
	val text: String,
	val tone: TermMetricDeltaTone
)
