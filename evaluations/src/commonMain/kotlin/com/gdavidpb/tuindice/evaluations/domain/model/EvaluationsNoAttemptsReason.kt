package com.gdavidpb.tuindice.evaluations.domain.model

import com.gdavidpb.tuindice.base.domain.model.EnrollmentAnnulmentCause

// Why there is no current term to list evaluations of. Annulled is the final annulment (the term
// is gone); a provisional one keeps the term and never reaches here.
sealed interface EvaluationsNoAttemptsReason {
	data object NoCurrentTerm : EvaluationsNoAttemptsReason

	data object EnrollmentUnavailable : EvaluationsNoAttemptsReason

	data object NotEnrolled : EvaluationsNoAttemptsReason

	data class Annulled(val cause: EnrollmentAnnulmentCause) : EvaluationsNoAttemptsReason
}
