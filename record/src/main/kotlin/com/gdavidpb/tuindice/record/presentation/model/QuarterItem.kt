package com.gdavidpb.tuindice.record.presentation.model

import androidx.compose.runtime.MutableIntState
import androidx.compose.ui.text.AnnotatedString

data class QuarterItem(
	val quarterId: String,
	val nameText: String,
	val gradeText: AnnotatedString,
	val gradeSumText: AnnotatedString,
	val creditsText: AnnotatedString,
	val subjects: List<SubjectItem>,
	val states: HashMap<String, MutableIntState>
)