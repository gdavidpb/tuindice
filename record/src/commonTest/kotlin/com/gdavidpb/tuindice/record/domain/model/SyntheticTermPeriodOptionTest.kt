package com.gdavidpb.tuindice.record.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyntheticTermPeriodOptionTest {
	@Test
	fun termKey_combinesYearAndPeriodName() {
		val option = SyntheticTermPeriodOption(
			periodYear = 2027,
			periodCode = AcademicTermPeriod.SEP_DEC
		)

		assertEquals("2027-SEP_DEC", option.termKey)
	}

	@Test
	fun termOrder_ranksPeriodsWithinTheSameYear() {
		val january = SyntheticTermPeriodOption(
			periodYear = 2027,
			periodCode = AcademicTermPeriod.JAN_MAR
		)
		val september = SyntheticTermPeriodOption(
			periodYear = 2027,
			periodCode = AcademicTermPeriod.SEP_DEC
		)
		val nextYear = SyntheticTermPeriodOption(
			periodYear = 2028,
			periodCode = AcademicTermPeriod.JAN_MAR
		)

		assertEquals(20271, january.termOrder)
		assertEquals(20274, september.termOrder)
		assertTrue(january.termOrder < september.termOrder)
		assertTrue(september.termOrder < nextYear.termOrder)
	}

	@Test
	fun label_usesShortPeriodLabelAndYear() {
		val option = SyntheticTermPeriodOption(
			periodYear = 2027,
			periodCode = AcademicTermPeriod.APR_JUL
		)

		assertEquals("Abr - Jul 2027", option.label)
	}

	@Test
	fun longPeriods_areNotSupportedForSyntheticPlanning() {
		assertTrue(!AcademicTermPeriod.JUL_DEC.supportsSyntheticPlanning)
		assertTrue(!AcademicTermPeriod.APR_SEP.supportsSyntheticPlanning)
	}
}
