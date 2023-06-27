package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.dateGroup

class EvaluationDateFilter(
	private val date: String
) : EvaluationFilter {
	override fun getLabel(): String {
		return date
	}

	override fun filter(evaluations: List<Evaluation>): List<Evaluation> {
		return evaluations.filter { evaluation ->
			date == evaluation.date.dateGroup()
		}
	}
}