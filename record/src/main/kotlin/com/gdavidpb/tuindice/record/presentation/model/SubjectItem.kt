package com.gdavidpb.tuindice.record.presentation.model

import com.gdavidpb.tuindice.base.domain.model.subject.Subject

data class SubjectItem(
	val uid: Long,
	val id: String,
	val code: String,
	val isRetired: Boolean,
	val nameText: CharSequence,
	val codeText: CharSequence,
	val gradeText: CharSequence,
	val creditsText: CharSequence,
	val data: Subject
)