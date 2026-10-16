package com.gdavidpb.tuindice.academiccore.domain.model

/**
 * What the evaluations of one attempt add up to: [earned] points out of [evaluated] points among the
 * [gradedCount] evaluations that have a grade, of [totalCount] evaluations in all.
 */
data class EvaluationPoints(
	val earned: Double,
	val evaluated: Double,
	val gradedCount: Int,
	val totalCount: Int
)
