package com.gdavidpb.tuindice.record.domain.model

data class QuarterAdd(
	val quarter: Int,
	val year: Int,
	val subjects: List<QuarterAddSubject>
)
