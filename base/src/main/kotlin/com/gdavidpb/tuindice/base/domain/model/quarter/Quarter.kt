package com.gdavidpb.tuindice.base.domain.model.quarter

import com.gdavidpb.tuindice.base.domain.model.subject.Subject

data class Quarter(
	val id: String,
	val name: String,
	val startDate: Long,
	val endDate: Long,
	val grade: Double,
	val gradeSum: Double,
	val credits: Int,
	val creditsSum: Int,
	val isCurrent: Boolean,
	val isReadOnly: Boolean,
	val subjects: List<Subject>
)