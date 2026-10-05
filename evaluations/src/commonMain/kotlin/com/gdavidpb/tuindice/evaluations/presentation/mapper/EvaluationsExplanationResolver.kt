package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.EnrollmentAnnulmentTexts
import com.gdavidpb.tuindice.base.presentation.mapper.NewStudentNoRecordTexts
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
// app, so all of them read under the calm illustration. An annulment that reaches here is the
// final one: the provisional keeps the term and shows as a notice above the evaluations.
internal fun resolveNoAttemptsExplanation(reason: EvaluationsNoAttemptsReason): EvaluationsExplanation {
	return when (reason) {
		EvaluationsNoAttemptsReason.NoCurrentTerm -> EvaluationsExplanation(
			title = UiText.Resource(Res.string.title_no_subjects_evaluations),
			message = UiText.Resource(Res.string.message_no_subjects_evaluations),
			illustration = EvaluationsIllustration.Empty
		)

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

		is EvaluationsNoAttemptsReason.Annulled -> EvaluationsExplanation(
			title = EnrollmentAnnulmentTexts.title(isProvisional = false),
			message = EnrollmentAnnulmentTexts.message(cause = reason.cause, isProvisional = false),
			illustration = EvaluationsIllustration.Empty
		)
	}
}

// The record the evaluations hang from is missing. For a new student the university has none yet:
// nothing failed, so it reads the shared new-student copy without the error art.
internal fun resolveRecordDataUnavailableExplanation(isNewStudentNoRecord: Boolean): EvaluationsExplanation {
	return if (isNewStudentNoRecord) {
		EvaluationsExplanation(
			title = NewStudentNoRecordTexts.title,
			message = NewStudentNoRecordTexts.message,
			illustration = EvaluationsIllustration.Empty
		)
	} else {
		EvaluationsExplanation(
			title = UiText.Resource(Res.string.title_record_unavailable_evaluations),
			message = UiText.Resource(Res.string.message_record_unavailable_evaluations),
			illustration = EvaluationsIllustration.Error
		)
	}
}
