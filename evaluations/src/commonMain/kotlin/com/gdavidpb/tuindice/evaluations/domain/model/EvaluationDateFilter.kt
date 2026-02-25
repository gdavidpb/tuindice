package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.base.domain.model.Evaluation

class EvaluationDateFilter(
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

		return label == other.label
	}

	override fun hashCode(): Int {
		return label.hashCode()
	}
}
