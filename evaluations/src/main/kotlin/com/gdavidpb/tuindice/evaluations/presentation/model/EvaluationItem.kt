package com.gdavidpb.tuindice.evaluations.presentation.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class EvaluationItem(
	val evaluationId: String,
	val grade: Double?,
	val maxGrade: Double,
	val nameText: String,
	val subjectCodeText: String,
	val highlightIconColor: Color,
	val highlightTextColor: Color,
	val typeAndSubjectCodeText: String,
	val typeIcon: ImageVector,
	val dateText: String,
	val dateIcon: ImageVector,
	val gradesText: String,
	val gradesIcon: ImageVector,
	val isOverdue: Boolean,
	val isClickable: Boolean
)