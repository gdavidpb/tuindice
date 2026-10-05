package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.EnrollmentAnnulmentCause
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
import kotlin.test.Test
import kotlin.test.assertEquals

class EvaluationsExplanationResolverTest {
	@Test
	fun resolveNoAttemptsExplanation_readsEachReasonWithItsOwnCopyUnderTheCalmArt() {
		assertEquals(
			EvaluationsExplanation(
				title = UiText.Resource(Res.string.title_no_subjects_evaluations),
				message = UiText.Resource(Res.string.message_no_subjects_evaluations),
				illustration = EvaluationsIllustration.Empty
			),
			resolveNoAttemptsExplanation(EvaluationsNoAttemptsReason.NoCurrentTerm)
		)
		assertEquals(
			EvaluationsExplanation(
				title = UiText.Resource(Res.string.title_enrollment_unavailable_evaluations),
				message = UiText.Resource(Res.string.message_enrollment_unavailable_evaluations),
				illustration = EvaluationsIllustration.Empty
			),
			resolveNoAttemptsExplanation(EvaluationsNoAttemptsReason.EnrollmentUnavailable)
		)
		assertEquals(
			EvaluationsExplanation(
				title = UiText.Resource(Res.string.title_not_enrolled_evaluations),
				message = UiText.Resource(Res.string.message_not_enrolled_evaluations),
				illustration = EvaluationsIllustration.Empty
			),
			resolveNoAttemptsExplanation(EvaluationsNoAttemptsReason.NotEnrolled)
		)
	}

	@Test
	fun resolveNoAttemptsExplanation_readsAnAnnulmentAsTheFinalOneWithItsCause() {
		val cause = EnrollmentAnnulmentCause.PermanenceRule

		assertEquals(
			EvaluationsExplanation(
				title = EnrollmentAnnulmentTexts.title(isProvisional = false),
				message = EnrollmentAnnulmentTexts.message(cause = cause, isProvisional = false),
				illustration = EvaluationsIllustration.Empty
			),
			resolveNoAttemptsExplanation(EvaluationsNoAttemptsReason.Annulled(cause = cause))
		)
	}

	@Test
	fun resolveRecordDataUnavailableExplanation_readsAFailedSyncAsAnError() {
		assertEquals(
			EvaluationsExplanation(
				title = UiText.Resource(Res.string.title_record_unavailable_evaluations),
				message = UiText.Resource(Res.string.message_record_unavailable_evaluations),
				illustration = EvaluationsIllustration.Error
			),
			resolveRecordDataUnavailableExplanation(isNewStudentNoRecord = false)
		)
	}

	@Test
	fun resolveRecordDataUnavailableExplanation_readsANewStudentWithTheSharedCopyAndNoErrorArt() {
		assertEquals(
			EvaluationsExplanation(
				title = NewStudentNoRecordTexts.title,
				message = NewStudentNoRecordTexts.message,
				illustration = EvaluationsIllustration.Empty
			),
			resolveRecordDataUnavailableExplanation(isNewStudentNoRecord = true)
		)
	}
}
