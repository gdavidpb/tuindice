package com.gdavidpb.tuindice.record.presentation.model

import androidx.compose.ui.text.AnnotatedString

data class QuarterItem(
	val quarterId: String,
	val shortNameText: String,
	val gradeText: AnnotatedString,
	val gradeSumText: AnnotatedString,
	val creditsText: AnnotatedString,
	val isCurrent: Boolean,
	val canDelete: Boolean,
	val subjects: List<SubjectItem>
)
