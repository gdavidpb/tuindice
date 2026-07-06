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

	@Test
	fun startMonthAndEndMonth_matchTheMonthsEachPeriodSpans() {
		val expectedRanges = mapOf(
			AcademicTermPeriod.JAN_MAR to (1 to 3),
			AcademicTermPeriod.JAN_MAY to (1 to 5),
			AcademicTermPeriod.APR_JUL to (4 to 7),
			AcademicTermPeriod.JUL_AUG to (7 to 8),
			AcademicTermPeriod.APR_SEP to (4 to 9),
			AcademicTermPeriod.SEP_DEC to (9 to 12),
			AcademicTermPeriod.JUL_DEC to (7 to 12)
		)

		for ((period, range) in expectedRanges) {
			val (start, end) = range

			assertEquals(start, period.startMonth, "startMonth of $period")
			assertEquals(end, period.endMonth, "endMonth of $period")
		}
	}
}
