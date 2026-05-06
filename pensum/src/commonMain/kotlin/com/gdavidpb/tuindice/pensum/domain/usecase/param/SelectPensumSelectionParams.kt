package com.gdavidpb.tuindice.pensum.domain.usecase.param

data class SelectPensumSelectionParams(
	val careerCode: Int,
	val year: Int,
	val modalityId: String
)
