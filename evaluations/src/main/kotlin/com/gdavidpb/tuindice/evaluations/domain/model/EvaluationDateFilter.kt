package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.dateGroup

class EvaluationDateFilter(
	private val date: String
) : EvaluationFilter {
	override fun getLabel(): String {
		return date
	}

	override fun filter(evaluation: Evaluation): Boolean {
		return date == evaluation.date.dateGroup()
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as EvaluationDateFilter

		if (date != other.date) return false

		return true
	}

	override fun hashCode(): Int {
		return date.hashCode()
	}
}