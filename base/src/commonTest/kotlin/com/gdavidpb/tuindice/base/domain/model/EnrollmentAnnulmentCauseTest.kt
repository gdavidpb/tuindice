package com.gdavidpb.tuindice.base.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class EnrollmentAnnulmentCauseTest {
	@Test
	fun fromCode_mapsTheKnownCodes() {
		assertEquals(EnrollmentAnnulmentCause.CreditLimit, EnrollmentAnnulmentCause.fromCode("01"))
		assertEquals(EnrollmentAnnulmentCause.AcademicIndex, EnrollmentAnnulmentCause.fromCode("06"))
		assertEquals(EnrollmentAnnulmentCause.WithdrawalRule, EnrollmentAnnulmentCause.fromCode("10"))
		assertEquals(EnrollmentAnnulmentCause.Probation, EnrollmentAnnulmentCause.fromCode("12"))
		assertEquals(EnrollmentAnnulmentCause.PermanenceRule, EnrollmentAnnulmentCause.fromCode("15"))
	}

	@Test
	fun fromCode_readsAnythingElseAsOther() {
		assertEquals(EnrollmentAnnulmentCause.Other, EnrollmentAnnulmentCause.fromCode(null))
		assertEquals(EnrollmentAnnulmentCause.Other, EnrollmentAnnulmentCause.fromCode("99"))
		assertEquals(EnrollmentAnnulmentCause.Other, EnrollmentAnnulmentCause.fromCode(""))
	}

	@Test
	fun situation_exposesItsCause() {
		assertEquals(EnrollmentAnnulmentCause.Probation, EnrollmentSituation(code = "12").annulmentCause)
		assertEquals(EnrollmentAnnulmentCause.Other, EnrollmentSituation().annulmentCause)
	}
}
