package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationsNoAttemptsReason
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsExplanation
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsIllustration
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.message_enrollment_unavailable_evaluations
import tuindice.evaluations.generated.resources.message_no_subjects_evaluations
import tuindice.evaluations.generated.resources.message_not_enrolled_evaluations
import tuindice.evaluations.generated.resources.message_record_unavailable_evaluations
import tuindice.evaluations.generated.resources.title_enrollment_unavailable_evaluations
import tuindice.evaluations.generated.resources.title_no_subjects_evaluations
import tuindice.evaluations.generated.resources.title_not_enrolled_evaluations
import tuindice.evaluations.generated.resources.title_record_unavailable_evaluations

// Why there is no current term to list evaluations of. None of the reasons is a failure of the
// app, so all of them read under the calm illustration.
internal fun resolveNoAttemptsExplanation(reason: EvaluationsNoAttemptsReason): EvaluationsExplanation {
	return when (reason) {
		EvaluationsNoAttemptsReason.NoCurrentTerm -> noCurrentTermExplanation()

		EvaluationsNoAttemptsReason.EnrollmentUnavailable -> EvaluationsExplanation(
			title = UiText.Resource(Res.string.title_enrollment_unavailable_evaluations),
			message = UiText.Resource(Res.string.message_enrollment_unavailable_evaluations),
			illustration = EvaluationsIllustration.Empty
		)

		EvaluationsNoAttemptsReason.NotEnrolled -> EvaluationsExplanation(
			title = UiText.Resource(Res.string.title_not_enrolled_evaluations),
			message = UiText.Resource(Res.string.message_not_enrolled_evaluations),
			illustration = EvaluationsIllustration.Empty
		)
	}
}

// The record the evaluations hang from is missing. For a new student the university has none yet:
// nothing failed, and what this screen has to say about it is that there is no current term.
internal fun resolveRecordDataUnavailableExplanation(isNewStudentNoRecord: Boolean): EvaluationsExplanation {
	return if (isNewStudentNoRecord) {
		noCurrentTermExplanation()
	} else {
		EvaluationsExplanation(
			title = UiText.Resource(Res.string.title_record_unavailable_evaluations),
			message = UiText.Resource(Res.string.message_record_unavailable_evaluations),
			illustration = EvaluationsIllustration.Error
		)
	}
}

private fun noCurrentTermExplanation() = EvaluationsExplanation(
	title = UiText.Resource(Res.string.title_no_subjects_evaluations),
	message = UiText.Resource(Res.string.message_no_subjects_evaluations),
	illustration = EvaluationsIllustration.Empty
)
