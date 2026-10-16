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

		// The range starts on March 30th, but the term starts in April: the picker opens on April.
		assertEquals(LocalDate(2026, 4, 1), LocalDate(2026, 1, 20).clampToMonthRange(range))
		assertEquals(LocalDate(2026, 8, 1), LocalDate(2026, 12, 20).clampToMonthRange(range))
		assertEquals(LocalDate(2026, 6, 1), LocalDate(2026, 6, 15).clampToMonthRange(range))
	}

	@Test
	fun clampToMonthRange_neverOpensBeforeTheMonthTheTermStartsIn_evenForADayOfWeekOne() {
		val range = term(AcademicTermPeriod.APR_JUL, year = 2026).toSelectableDateRange()

		// March 30th and 31st are selectable (week 1), but March only has those days enabled.
		assertEquals(LocalDate(2026, 4, 1), LocalDate(2026, 3, 30).clampToMonthRange(range))
		assertEquals(LocalDate(2026, 4, 1), LocalDate(2026, 3, 31).clampToMonthRange(range))
		// Going back to March is still possible: the range keeps its days.
		assertTrue(LocalDate(2026, 3, 30) in range)
		assertEquals(LocalDate(2026, 3, 1), range.start.monthStart())
	}

	@Test
	fun clampToMonthRange_whenTheRangeStartsOnTheFirst_opensOnThatMonth() {
		val range = term(AcademicTermPeriod.SEP_DEC, year = 2026)
			.copy(classesStartDate = LocalDate(2026, 9, 28))
			.toSelectableDateRange()

		assertEquals(LocalDate(2026, 9, 1), LocalDate(2026, 1, 20).clampToMonthRange(range))
		assertEquals(LocalDate(2026, 9, 1), LocalDate(2026, 9, 20).clampToMonthRange(range))
	}

	@Test
	fun initialPickerMonth_opensOnTheMonthOfTheChosenDate_evenWhenItIsBeforeTheMonthTheTermStartsIn() {
		val range = term(AcademicTermPeriod.APR_JUL, year = 2026).toSelectableDateRange()
		val today = LocalDate(2026, 5, 12)

		// March 30th is day 1 of week 1 of an April term: the picker opens on March, day in view.
		assertEquals(LocalDate(2026, 3, 1), initialPickerMonth(LocalDate(2026, 3, 30), today, range))
		assertEquals(LocalDate(2026, 6, 1), initialPickerMonth(LocalDate(2026, 6, 15), today, range))
	}

	@Test
	fun initialPickerMonth_withoutAChosenDate_keepsTheClampedRuleForToday() {
		val range = term(AcademicTermPeriod.APR_JUL, year = 2026).toSelectableDateRange()

		// Today inside the range but before the term's start month still opens on April.
		assertEquals(LocalDate(2026, 4, 1), initialPickerMonth(null, LocalDate(2026, 3, 31), range))
		assertEquals(LocalDate(2026, 4, 1), initialPickerMonth(null, LocalDate(2026, 1, 20), range))
		assertEquals(LocalDate(2026, 5, 1), initialPickerMonth(null, LocalDate(2026, 5, 12), range))
	}

	@Test
	fun initialPickerMonth_withAChosenDateOutsideTheRange_snapsItIntoTheRange() {
		val range = term(AcademicTermPeriod.APR_JUL, year = 2026).toSelectableDateRange()

		assertEquals(LocalDate(2026, 4, 1), initialPickerMonth(LocalDate(2026, 1, 20), LocalDate(2026, 5, 12), range))
		assertEquals(LocalDate(2026, 8, 1), initialPickerMonth(LocalDate(2026, 12, 20), LocalDate(2026, 5, 12), range))
	}

	@Test
	fun initialPickerMonth_withoutARange_opensOnTheMonthOfTheDate() {
		assertEquals(LocalDate(2026, 3, 1), initialPickerMonth(LocalDate(2026, 3, 30), LocalDate(2026, 5, 12), null))
		assertEquals(LocalDate(2026, 5, 1), initialPickerMonth(null, LocalDate(2026, 5, 12), null))
	}

	private fun term(period: AcademicTermPeriod, year: Int) = EvaluationTermDescriptor(
		id = "$year-${period.name}",
		periodYear = year,
		periodCode = period,
		periodLabel = "${period.label} $year"
	)
}
