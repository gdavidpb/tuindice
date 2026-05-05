package com.gdavidpb.tuindice.pensum.domain.model

data class PensumSelection(
	val pensumId: String,
	val careerCode: Int,
	val careerName: String,
	val year: Int,
	val modalityId: String,
	val modalityName: String,
	val inferred: Boolean
)
