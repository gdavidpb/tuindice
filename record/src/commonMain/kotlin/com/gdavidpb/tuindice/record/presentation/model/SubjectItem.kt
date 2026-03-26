package com.gdavidpb.tuindice.record.presentation.model

import androidx.compose.ui.graphics.Color

data class SubjectItem(
	val subjectId: String,
	val quarterId: String,
	val grade: Int,
	val codeText: String,
	val nameText: String,
	val gradeText: String,
	val creditsText: String,
	val codeColor: Color,
	val codeContainerColor: Color,
	val isReadOnly: Boolean
)
