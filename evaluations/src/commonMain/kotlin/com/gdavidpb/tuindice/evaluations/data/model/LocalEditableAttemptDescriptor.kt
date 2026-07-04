package com.gdavidpb.tuindice.evaluations.data.model

import com.gdavidpb.tuindice.academiccore.domain.model.GradingMode

data class LocalEditableAttemptDescriptor(
	val id: String,
	val termId: String,
	val code: String,
	val name: String,
	val credits: Int,
	val grade: Int,
	val gradingMode: GradingMode = GradingMode.NUMERIC
)
