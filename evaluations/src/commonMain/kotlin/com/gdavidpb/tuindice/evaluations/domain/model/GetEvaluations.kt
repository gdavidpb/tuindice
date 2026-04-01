package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.base.domain.model.Evaluation

sealed interface GetEvaluations {
	data object NoSubjects : GetEvaluations

	data class Content(
		val originalEvaluations: List<Evaluation>,
		val filteredEvaluations: List<Evaluation>,
		val activeFilters: List<EvaluationFilter>
	) : GetEvaluations
}
