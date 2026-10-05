package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.EnrollmentAnnulmentTexts
import com.gdavidpb.tuindice.evaluations.domain.model.GetEvaluations
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsNotice

// The notice above the evaluations. Content is only observed with a current term, so an annulment
// seen from here is always the provisional one; the final one has dropped the term and arrives as
// NoAttempts instead.
internal fun resolveEvaluationsNotice(observed: GetEvaluations.Content): EvaluationsNotice? {
	val situation = observed.enrollmentSituation ?: return null

	return EvaluationsNotice(
		title = EnrollmentAnnulmentTexts.title(isProvisional = true),
		message = EnrollmentAnnulmentTexts.message(
			cause = situation.annulmentCause,
			isProvisional = true
		)
	)
}
