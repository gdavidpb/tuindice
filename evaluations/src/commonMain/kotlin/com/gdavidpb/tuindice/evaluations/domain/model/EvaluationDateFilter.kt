package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.base.domain.model.Evaluation

class EvaluationDateFilter(
	private val group: EvaluationDateGroup,
	private val label: String,
	private val selector: (Evaluation) -> Boolean
) : EvaluationFilter {
	override fun getLabel(): String {
		return label
	}

	override fun match(evaluation: Evaluation): Boolean {
		return selector(evaluation)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is EvaluationDateFilter) return false

		return group == other.group
	}

	override fun hashCode(): Int {
		return group.hashCode()
	}
}
