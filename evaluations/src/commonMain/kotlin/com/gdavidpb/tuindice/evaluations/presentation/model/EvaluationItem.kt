package com.gdavidpb.tuindice.evaluations.presentation.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class EvaluationItem(
	val evaluationId: String,
	val grade: Double?,
	val maxGrade: Double,
	val nameText: String,
	val subjectNameText: String,
	val subjectCodeText: String,
	val subjectCodeColor: Color,
	val subjectCodeContainerColor: Color,
	val highlightTone: EvaluationHighlightTone,
	val statusText: String,
	val statusTone: EvaluationHighlightTone,
	val typeText: String,
	val typeNameText: String,
	val typeIcon: ImageVector,
	val dateText: String,
	val dateIcon: ImageVector,
	val gradeText: String,
	val gradesText: String,
	val gradeActionText: String,
	val showsGradeAction: Boolean,
	val gradesIcon: ImageVector,
	val isOverdue: Boolean,
	val isClickable: Boolean
)
