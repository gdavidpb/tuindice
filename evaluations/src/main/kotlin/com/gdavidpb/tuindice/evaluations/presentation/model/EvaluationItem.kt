package com.gdavidpb.tuindice.evaluations.presentation.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class EvaluationItem(
	val evaluationId: String,
	val name: String,
	val subjectCode: String,
	val grade: Double,
	val maxGrade: Double,
	val highlightIconColor: Color,
	val highlightTextColor: Color,
	val typeAndSubjectCode: String,
	val typeIcon: ImageVector,
	val date: String,
	val dateIcon: ImageVector,
	val grades: String,
	val gradesIcon: ImageVector,
	val isGradeRequired: Boolean
)