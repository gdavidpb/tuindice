package com.gdavidpb.tuindice.evaluations.presentation.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.gdavidpb.tuindice.base.domain.model.EvaluationType

data class EvaluationTypePickerItem(
	val type: EvaluationType,
	val labelText: String,
	val icon: ImageVector,
	val isSelected: Boolean,
	val isVisible: Boolean
)
