package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.EnrollmentAnnulmentCause
import com.gdavidpb.tuindice.base.domain.model.EnrollmentSituation
import com.gdavidpb.tuindice.base.presentation.mapper.EnrollmentAnnulmentTexts
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDisplayContext
import com.gdavidpb.tuindice.evaluations.domain.model.GetEvaluations
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsNotice
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_TERM
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EvaluationsNoticeResolverTest {
	@Test
	fun resolveEvaluationsNotice_isNullWhileTheUniversityReportsNoAnnulment() {
		assertNull(resolveEvaluationsNotice(observed = content(enrollmentSituation = null)))
	}

	@Test
	fun resolveEvaluationsNotice_readsAnAnnulmentAsTheProvisionalOneWithItsCause() {
		val situation = EnrollmentSituation(code = "01")

		assertEquals(EnrollmentAnnulmentCause.CreditLimit, situation.annulmentCause)
		assertEquals(
			EvaluationsNotice(
				title = EnrollmentAnnulmentTexts.title(isProvisional = true),
				message = EnrollmentAnnulmentTexts.message(
					cause = EnrollmentAnnulmentCause.CreditLimit,
					isProvisional = true
				)
			),
			resolveEvaluationsNotice(observed = content(enrollmentSituation = situation))
		)
	}

	private fun content(enrollmentSituation: EnrollmentSituation?) = GetEvaluations.Content(
		evaluations = listOf(DEFAULT_PENDING_EVALUATION),
		hasSyncedEvaluations = true,
		displayContext = EvaluationDisplayContext(
			attempts = listOf(DEFAULT_EVALUATION_SUBJECT),
			currentTerm = DEFAULT_EVALUATION_TERM
		),
		selectedWeekKey = null,
		enrollmentSituation = enrollmentSituation
	)
}
