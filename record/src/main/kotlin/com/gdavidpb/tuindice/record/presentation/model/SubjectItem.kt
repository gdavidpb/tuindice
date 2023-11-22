package com.gdavidpb.tuindice.record.presentation.model

import androidx.compose.ui.text.AnnotatedString

data class SubjectItem(
	val subjectId: String,
	val quarterId: String,
	val grade: Int,
	val codeAndStatusText: AnnotatedString,
	val nameText: String,
	val gradeText: String,
	val creditsText: String,
	val isRetired: Boolean,
	val isNoEffect: Boolean,
	val isEditable: Boolean
)