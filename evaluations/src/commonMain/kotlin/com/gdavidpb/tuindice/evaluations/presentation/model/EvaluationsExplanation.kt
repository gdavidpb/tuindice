package com.gdavidpb.tuindice.evaluations.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// What a state with no evaluations to list says instead, already resolved to text.
data class EvaluationsExplanation(
	val title: UiText,
	val message: UiText,
	val illustration: EvaluationsIllustration
)
