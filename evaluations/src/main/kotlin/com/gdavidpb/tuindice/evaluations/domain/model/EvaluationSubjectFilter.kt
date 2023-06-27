package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.base.domain.model.Evaluation

class EvaluationSubjectFilter(
	private val subjectCode: String
) : EvaluationFilter {
	override fun getLabel(): String {
		return subjectCode
	}

	override fun filter(evaluations: List<Evaluation>): List<Evaluation> {
		return evaluations.filter { evaluation ->
			subjectCode == evaluation.subjectName
		}
	}
}