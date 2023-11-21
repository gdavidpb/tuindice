package com.gdavidpb.tuindice.base.domain.model.quarter

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import java.util.*

data class Quarter(
	val id: String,
	val name: String,
	val startDate: Long,
	val endDate: Long,
	val grade: Double,
	val gradeSum: Double,
	val credits: Int,
	val status: Int,
	val isEditable: Boolean,
	val isRetired: Boolean,
	val subjects: List<Subject>
)