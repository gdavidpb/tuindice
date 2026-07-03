package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation

sealed interface GetEvaluations {
	data object WaitingForRecordData : GetEvaluations

	data object RecordDataUnavailable : GetEvaluations

	data class NoAttempts(
		val reason: EvaluationsNoAttemptsReason
	) : GetEvaluations

	data class Content(
		val evaluations: List<Evaluation>,
		val hasSyncedEvaluations: Boolean,
		val displayContext: EvaluationDisplayContext
	) : GetEvaluations
}

enum class EvaluationsNoAttemptsReason {
	NoCurrentTerm,
	EnrollmentUnavailable
}
