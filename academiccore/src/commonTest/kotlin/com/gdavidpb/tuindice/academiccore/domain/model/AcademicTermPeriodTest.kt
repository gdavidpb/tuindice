package com.gdavidpb.tuindice.academiccore.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicTermPeriodTest {
	@Test
	fun termOrder_sortsRegularAndLongPeriodsChronologicallyWithinYear() {
		val terms = listOf(
			AcademicTermPeriod.JUL_DEC,
			AcademicTermPeriod.SEP_DEC,
			AcademicTermPeriod.JUL_AUG,
			AcademicTermPeriod.APR_SEP,
			AcademicTermPeriod.APR_JUL,
			AcademicTermPeriod.JAN_MAY,
			AcademicTermPeriod.JAN_MAR
		).map { period ->
			AcademicTerm(
				id = "2027-${period.name}",
				periodYear = 2027,
				periodCode = period,
				kind = TermKind.HISTORICAL
			)
		}

		val sortedTerms = terms.sortedBy(AcademicTerm::termOrder)

		assertEquals(
			listOf(
				AcademicTermPeriod.JAN_MAR,
				AcademicTermPeriod.JAN_MAY,
				AcademicTermPeriod.APR_JUL,
				AcademicTermPeriod.JUL_AUG,
				AcademicTermPeriod.APR_SEP,
				AcademicTermPeriod.SEP_DEC,
				AcademicTermPeriod.JUL_DEC
			),
			sortedTerms.map(AcademicTerm::periodCode)
		)
		assertEquals(
			listOf(20271, 20272, 20273, 20274, 20275, 20276, 20277),
			sortedTerms.map(AcademicTerm::termOrder)
		)
	}
}
