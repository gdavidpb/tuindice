package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation

sealed interface GetEvaluations {
	data object WaitingForRecordData : GetEvaluations

	// isNewStudentNoRecord: the record is missing because the university has none for this
	// account yet, not because the sync broke.
	data class RecordDataUnavailable(
		val isNewStudentNoRecord: Boolean = false
	) : GetEvaluations

	data class NoAttempts(
		val reason: EvaluationsNoAttemptsReason
	) : GetEvaluations

	data class Content(
		val evaluations: List<Evaluation>,
		val hasSyncedEvaluations: Boolean,
		val displayContext: EvaluationDisplayContext,
		val selectedWeekKey: String?
	) : GetEvaluations
}
