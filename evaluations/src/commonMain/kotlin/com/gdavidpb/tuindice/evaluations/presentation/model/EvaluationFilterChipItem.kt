package com.gdavidpb.tuindice.evaluations.presentation.model

import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter

data class EvaluationFilterChipItem(
	val filter: EvaluationFilter,
	val labelText: String,
	val isChecked: Boolean,
	val contentColor: Color? = null,
	val containerColor: Color? = null,
	val emphasizeWhenChecked: Boolean = false
)
