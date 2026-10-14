package com.gdavidpb.tuindice.academiccore.domain.model

/** The credits of the subjects in each state of [SubjectOutcomes]. Each subject adds its credits once. */
data class CreditOutcomes(
	val approved: Int,
	val failed: Int,
	val withdrawn: Int
)
