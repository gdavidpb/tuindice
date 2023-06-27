package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.base.domain.model.Evaluation

class EvaluationStateFilter(
	private val label: String,
	private val selector: (Evaluation) -> Boolean
) : EvaluationFilter {
	override fun getLabel(): String {
		return label
	}

	override fun filter(evaluations: List<Evaluation>): List<Evaluation> {
		return evaluations.filter { evaluation ->
			selector(evaluation)
		}
	}
}