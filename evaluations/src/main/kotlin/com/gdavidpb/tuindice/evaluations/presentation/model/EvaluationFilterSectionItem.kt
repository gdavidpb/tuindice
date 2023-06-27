package com.gdavidpb.tuindice.evaluations.presentation.model

import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.vector.ImageVector
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter

data class EvaluationFilterSectionItem(
	val name: String,
	val icon: ImageVector,
	val entries: Map<EvaluationFilter, MutableState<Boolean>>
)