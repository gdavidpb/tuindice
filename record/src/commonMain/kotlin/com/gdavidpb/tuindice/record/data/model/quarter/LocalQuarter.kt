package com.gdavidpb.tuindice.record.data.model.quarter

data class LocalQuarter(
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
	val revision: Long,
	val subjects: List<LocalSubject>
)
