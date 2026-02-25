package com.gdavidpb.tuindice.record.data.repository.quarter.model

data class RemoteQuarter(
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
	val subjects: List<RemoteSubject>
)