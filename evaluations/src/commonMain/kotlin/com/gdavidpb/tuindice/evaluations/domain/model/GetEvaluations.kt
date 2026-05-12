package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.base.domain.model.Evaluation

sealed interface GetEvaluations {
	data object WaitingForRecordData : GetEvaluations

	data object RecordDataUnavailable : GetEvaluations

	data object NoAttempts : GetEvaluations

	data class Content(
		val originalEvaluations: List<Evaluation>,
		val filteredEvaluations: List<Evaluation>,
		val activeFilters: List<EvaluationFilter>,
		val hasSyncedEvaluations: Boolean
	) : GetEvaluations
}
