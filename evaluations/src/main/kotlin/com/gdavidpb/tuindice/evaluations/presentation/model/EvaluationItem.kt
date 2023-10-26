package com.gdavidpb.tuindice.evaluations.presentation.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class EvaluationItem(
	val evaluationId: String,
	val name: String,
	val subjectCode: String,
	val typeAndSubjectCode: String,
	val typeIcon: ImageVector,
	val date: String,
	val dateColor: Color,
	val dateIcon: ImageVector,
	val dateIconColor: Color,
	val grades: String,
	val isDone: Boolean,
	val isCompleted: Boolean,
	val isGradeRequired: Boolean
)