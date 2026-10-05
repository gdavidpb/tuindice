package com.gdavidpb.tuindice.evaluations.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// A calm explanation shown above the evaluations, already resolved to text.
data class EvaluationsNotice(
	val title: UiText,
	val message: UiText
)
