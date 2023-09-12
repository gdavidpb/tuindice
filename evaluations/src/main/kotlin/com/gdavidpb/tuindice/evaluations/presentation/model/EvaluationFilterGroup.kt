package com.gdavidpb.tuindice.evaluations.presentation.model

import androidx.compose.runtime.MutableState
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter

data class EvaluationFilterGroup(
	val entries: Map<EvaluationFilter, MutableState<Boolean>>
)