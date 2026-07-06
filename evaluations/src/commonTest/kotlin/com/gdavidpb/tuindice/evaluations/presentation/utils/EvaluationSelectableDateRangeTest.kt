package com.gdavidpb.tuindice.evaluations.presentation.utils

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EvaluationSelectableDateRangeTest {
	@Test
	fun toSelectableDateRange_spansTheTermPlusOneMonthOfSlack() {
		val range = term(AcademicTermPeriod.JAN_MAR, year = 2026).toSelectableDateRange()

		// January–March term → January 1st through April 30th.
		assertEquals(LocalDate(2026, 1, 1), range.start)
		assertEquals(LocalDate(2026, 4, 30), range.endInclusive)
	}

	@Test
	fun toSelectableDateRange_rollsOverTheYearWhenTheTermEndsInDecember() {
		val range = term(AcademicTermPeriod.SEP_DEC, year = 2026).toSelectableDateRange()

		// September–December term → September 1st through January 31st of next year.
		assertEquals(LocalDate(2026, 9, 1), range.start)
		assertEquals(LocalDate(2027, 1, 31), range.endInclusive)
	}

	@Test
	fun toSelectableDateRange_includesTheBoundsAndExcludesDatesOutside() {
		val range = term(AcademicTermPeriod.APR_JUL, year = 2026).toSelectableDateRange()

		assertTrue(LocalDate(2026, 4, 1) in range)
		assertTrue(LocalDate(2026, 8, 31) in range)
		assertFalse(LocalDate(2026, 3, 31) in range)
		assertFalse(LocalDate(2026, 9, 1) in range)
	}

	@Test
	fun clampToMonthRange_snapsOutOfRangeDatesToTheNearestAllowedMonth() {
		val range = term(AcademicTermPeriod.APR_JUL, year = 2026).toSelectableDateRange()

		assertEquals(LocalDate(2026, 4, 1), LocalDate(2026, 1, 20).clampToMonthRange(range))
		assertEquals(LocalDate(2026, 8, 1), LocalDate(2026, 12, 20).clampToMonthRange(range))
		assertEquals(LocalDate(2026, 6, 1), LocalDate(2026, 6, 15).clampToMonthRange(range))
	}

	private fun term(period: AcademicTermPeriod, year: Int) = EvaluationTermDescriptor(
		id = "$year-${period.name}",
		periodYear = year,
		periodCode = period,
		periodLabel = "${period.label} $year"
	)
}
