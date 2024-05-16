package com.gdavidpb.tuindice.record.data.repository.quarter.model

data class LocalQuarter(
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
	val subjects: List<LocalSubject>
)