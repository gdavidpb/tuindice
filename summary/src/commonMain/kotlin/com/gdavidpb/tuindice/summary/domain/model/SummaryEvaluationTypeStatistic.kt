package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType

/**
 * The evaluations of one [type] in the term in progress: [gradedCount] of [totalCount] have a grade, and those
 * grades add up to [earned] points out of [evaluated], the maximum grades of those same graded evaluations.
 */
data class SummaryEvaluationTypeStatistic(
	val type: EvaluationType,
	val earned: Double,
	val evaluated: Double,
	val gradedCount: Int,
	val totalCount: Int
) {
	/** The weighted performance of the type: earned over evaluated, or null while none of its evaluations is graded. */
	val performance: Double?
		get() = evaluated.takeIf { points -> points > 0.0 }?.let { points -> earned / points }
}
