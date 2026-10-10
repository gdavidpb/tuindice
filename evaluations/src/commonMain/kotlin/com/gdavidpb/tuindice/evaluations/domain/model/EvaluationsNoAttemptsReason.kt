package com.gdavidpb.tuindice.evaluations.domain.model

// Why there is no current term to list evaluations of. A final annulment drops the term and reads
// as NoCurrentTerm: the record and the summary explain it, and this screen does not repeat them.
sealed interface EvaluationsNoAttemptsReason {
	data object NoCurrentTerm : EvaluationsNoAttemptsReason

	data object EnrollmentUnavailable : EvaluationsNoAttemptsReason

	data object NotEnrolled : EvaluationsNoAttemptsReason
}
