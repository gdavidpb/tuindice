package com.gdavidpb.tuindice.evaluations.presentation.model

import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor

data class EvaluationAttemptPickerItem(
	val attempt: EditableAttemptDescriptor,
	val labelText: String,
	val isSelected: Boolean,
	val isVisible: Boolean,
	val containerColor: Color,
	val contentColor: Color,
	val disabledContainerColor: Color,
	val disabledContentColor: Color
)
