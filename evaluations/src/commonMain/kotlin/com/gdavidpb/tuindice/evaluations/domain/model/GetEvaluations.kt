package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EnrollmentSituation

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
		val selectedWeekKey: String?,
		// Present while the university reports the enrollment as annulled but the current term is
		// still kept (the provisional moment): the evaluations stay usable and get a notice.
		val enrollmentSituation: EnrollmentSituation? = null
	) : GetEvaluations
}
