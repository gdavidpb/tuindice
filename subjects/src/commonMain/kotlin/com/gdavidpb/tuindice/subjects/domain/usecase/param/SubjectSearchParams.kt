package com.gdavidpb.tuindice.subjects.domain.usecase.param

data class SubjectSearchParams(
	val query: String,
	val limit: Int = 20
)
