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

		// January–March term → January 1st (a Thursday, so week 1 opens on Monday December 29th)
		// through April 30th.
		assertEquals(LocalDate(2025, 12, 29), range.start)
		assertEquals(LocalDate(2026, 4, 30), range.endInclusive)
	}

	@Test
	fun toSelectableDateRange_rollsOverTheYearWhenTheTermEndsInDecember() {
		val range = term(AcademicTermPeriod.SEP_DEC, year = 2026).toSelectableDateRange()

		// September–December term → from Monday August 31st (week 1) through January 31st of next year.
		assertEquals(LocalDate(2026, 8, 31), range.start)
		assertEquals(LocalDate(2027, 1, 31), range.endInclusive)
	}

	@Test
	fun toSelectableDateRange_includesTheBoundsAndExcludesDatesOutside() {
		val range = term(AcademicTermPeriod.APR_JUL, year = 2026).toSelectableDateRange()

		assertTrue(LocalDate(2026, 3, 30) in range)
		assertTrue(LocalDate(2026, 8, 31) in range)
		assertFalse(LocalDate(2026, 3, 29) in range)
		assertFalse(LocalDate(2026, 9, 1) in range)
	}

	@Test
	fun toSelectableDateRange_reachesBackToTheMondayOfWeekOneWhenClassesStartInThePreviousMonth() {
		// April 2026 starts on a Wednesday: week 1 opens on Monday March 30th, and a real start on
		// the 31st does not move it.
		val range = term(AcademicTermPeriod.APR_JUL, year = 2026)
			.copy(classesStartDate = LocalDate(2026, 3, 31))
			.toSelectableDateRange()

		assertEquals(LocalDate(2026, 3, 30), range.start)
		assertEquals(LocalDate(2026, 8, 31), range.endInclusive)
	}

	@Test
	fun toSelectableDateRange_keepsTheFirstOfTheStartMonthWhenWeekOneOpensLater() {
		// Classes start on Monday September 28th: week 1 opens after the 1st, which stays the start.
		val range = term(AcademicTermPeriod.SEP_DEC, year = 2026)
			.copy(classesStartDate = LocalDate(2026, 9, 28))
			.toSelectableDateRange()

		assertEquals(LocalDate(2026, 9, 1), range.start)
		assertEquals(LocalDate(2027, 1, 31), range.endInclusive)
	}

	@Test
	fun clampToMonthRange_snapsOutOfRangeDatesToTheNearestAllowedMonth() {
		val range = term(AcademicTermPeriod.APR_JUL, year = 2026).toSelectableDateRange()

		// The range starts on March 30th, so the month it snaps to is March.
		assertEquals(LocalDate(2026, 3, 1), LocalDate(2026, 1, 20).clampToMonthRange(range))
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
