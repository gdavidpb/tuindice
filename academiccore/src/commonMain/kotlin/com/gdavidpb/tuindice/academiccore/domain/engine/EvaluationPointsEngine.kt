package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationPoints

/** The points of the evaluations of each attempt. */
object EvaluationPointsEngine {
	/**
	 * Per attempt id, the points earned (sum of [Evaluation.grade] of the graded evaluations), the
	 * points evaluated (sum of [Evaluation.maxGrade] of those same evaluations) and how many are
	 * graded out of how many there are. An attempt with no evaluation has no entry.
	 */
	fun pointsByAttempt(evaluations: List<Evaluation>): Map<String, EvaluationPoints> {
		return evaluations
			.groupBy(Evaluation::attemptId)
			.mapValues { (_, attemptEvaluations) ->
				val graded = attemptEvaluations.filter { evaluation -> evaluation.grade != null }

				EvaluationPoints(
					earned = graded.sumOf { evaluation -> evaluation.grade ?: 0.0 },
					evaluated = graded.sumOf(Evaluation::maxGrade),
					gradedCount = graded.size,
					totalCount = attemptEvaluations.size
				)
			}
	}
}
