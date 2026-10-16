package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation

/** The number each evaluation has in its name ("Parcial 1", "Parcial 2"). */
object EvaluationOrdinalEngine {
	/**
	 * Per evaluation id, its place counted from 1 among the evaluations of the same attempt and the
	 * same type, by date (the ones with no date first, ties in the order given). It depends only on
	 * [evaluations], so a screen that shows some of them must pass all of those it counts among.
	 */
	fun ordinalsById(evaluations: List<Evaluation>): Map<String, Int> {
		return evaluations
			.sortedBy(Evaluation::date)
			.groupBy { evaluation -> evaluation.attemptId to evaluation.type }
			.values
			.flatMap { sameType ->
				sameType.mapIndexed { index, evaluation -> evaluation.id to index + 1 }
			}
			.toMap()
	}
}
