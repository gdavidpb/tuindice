package com.gdavidpb.tuindice.base.presentation.model

import com.gdavidpb.tuindice.base.domain.model.EnrollmentAnnulmentCause
import tuindice.base.generated.resources.Res
import tuindice.base.generated.resources.enrollment_annulled_cause_academic_index
import tuindice.base.generated.resources.enrollment_annulled_cause_credit_limit
import tuindice.base.generated.resources.enrollment_annulled_cause_permanence_rule
import tuindice.base.generated.resources.enrollment_annulled_cause_probation
import tuindice.base.generated.resources.enrollment_annulled_cause_withdrawal_rule
import tuindice.base.generated.resources.enrollment_annulled_final_message
import tuindice.base.generated.resources.enrollment_annulled_final_message_generic
import tuindice.base.generated.resources.enrollment_annulled_final_title
import tuindice.base.generated.resources.enrollment_annulled_provisional_message
import tuindice.base.generated.resources.enrollment_annulled_provisional_message_generic
import tuindice.base.generated.resources.enrollment_annulled_provisional_title

// The copy of an annulled enrollment, shared by every module that shows it. The moment is decided
// by whoever shows it: provisional while the record still has a current term, final once it does
// not. A cause this build does not know reads with the generic message.
object EnrollmentAnnulmentTexts {
	fun title(isProvisional: Boolean): UiText {
		return UiText.Resource(
			if (isProvisional) {
				Res.string.enrollment_annulled_provisional_title
			} else {
				Res.string.enrollment_annulled_final_title
			}
		)
	}

	fun message(cause: EnrollmentAnnulmentCause, isProvisional: Boolean): UiText {
		val causeText = causeText(cause)

		return when {
			isProvisional && causeText != null ->
				UiText.Resource(Res.string.enrollment_annulled_provisional_message, listOf(causeText))

			isProvisional ->
				UiText.Resource(Res.string.enrollment_annulled_provisional_message_generic)

			causeText != null ->
				UiText.Resource(Res.string.enrollment_annulled_final_message, listOf(causeText))

			else ->
				UiText.Resource(Res.string.enrollment_annulled_final_message_generic)
		}
	}

	private fun causeText(cause: EnrollmentAnnulmentCause): UiText? {
		val resource = when (cause) {
			EnrollmentAnnulmentCause.CreditLimit -> Res.string.enrollment_annulled_cause_credit_limit
			EnrollmentAnnulmentCause.AcademicIndex -> Res.string.enrollment_annulled_cause_academic_index
			EnrollmentAnnulmentCause.WithdrawalRule -> Res.string.enrollment_annulled_cause_withdrawal_rule
			EnrollmentAnnulmentCause.Probation -> Res.string.enrollment_annulled_cause_probation
			EnrollmentAnnulmentCause.PermanenceRule -> Res.string.enrollment_annulled_cause_permanence_rule
			EnrollmentAnnulmentCause.Other -> return null
		}

		return UiText.Resource(resource)
	}
}
