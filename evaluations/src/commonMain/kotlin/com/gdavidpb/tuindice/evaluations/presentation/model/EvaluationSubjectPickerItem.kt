package com.gdavidpb.tuindice.evaluations.presentation.model

import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.base.domain.model.subject.Subject

data class EvaluationSubjectPickerItem(
	val subject: Subject,
	val labelText: String,
	val isSelected: Boolean,
	val isVisible: Boolean,
	val containerColor: Color,
	val contentColor: Color,
	val disabledContainerColor: Color,
	val disabledContentColor: Color
)
