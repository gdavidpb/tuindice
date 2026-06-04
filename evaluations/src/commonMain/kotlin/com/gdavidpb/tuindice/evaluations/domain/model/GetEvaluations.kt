package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.base.domain.model.Evaluation

sealed interface GetEvaluations {
	data object WaitingForRecordData : GetEvaluations

	data object RecordDataUnavailable : GetEvaluations

	data object NoAttempts : GetEvaluations

	data class Content(
		val evaluations: List<Evaluation>,
		val hasSyncedEvaluations: Boolean,
		val displayContext: EvaluationDisplayContext
	) : GetEvaluations
}
