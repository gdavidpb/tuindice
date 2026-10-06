package com.gdavidpb.tuindice.evaluations.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

data class EvaluationsGroupItem(
	val title: UiText,
	val items: List<EvaluationItem>
)
