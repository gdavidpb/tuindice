package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.base.domain.model.GradingMode

data class EditableAttemptDescriptor(
	val id: String,
	val termId: String,
	val code: String,
	val name: String,
	val credits: Int,
	val grade: Int,
	val gradingMode: GradingMode = GradingMode.NUMERIC
)
