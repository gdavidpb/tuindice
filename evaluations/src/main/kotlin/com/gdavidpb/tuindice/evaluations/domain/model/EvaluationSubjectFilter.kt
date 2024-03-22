package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.base.domain.model.Evaluation

class EvaluationSubjectFilter(
	private val subjectCode: String
) : EvaluationFilter {
	override fun getLabel(): String {
		return subjectCode
	}

	override fun match(evaluation: Evaluation): Boolean {
		return subjectCode == evaluation.subjectCode
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as EvaluationSubjectFilter

		return subjectCode == other.subjectCode
	}

	override fun hashCode(): Int {
		return subjectCode.hashCode()
	}
}