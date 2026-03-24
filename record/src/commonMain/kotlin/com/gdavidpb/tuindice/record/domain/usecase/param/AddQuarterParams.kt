package com.gdavidpb.tuindice.record.domain.usecase.param

data class AddQuarterParams(
	val quarter: Int,
	val year: Int,
	val subjects: List<AddQuarterSubjectParams>
)
