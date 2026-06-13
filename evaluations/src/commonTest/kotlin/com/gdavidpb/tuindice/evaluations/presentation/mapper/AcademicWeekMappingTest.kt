package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_TERM
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicWeekMappingTest {
	@Test
	fun academicTermStartDate_whenPeriodVaries_startsOnFirstDayOfPeriodMonth() {
		assertEquals(
			LocalDate(2026, 1, 1),
			DEFAULT_EVALUATION_TERM.copy(periodCode = AcademicTermPeriod.JAN_MAR).academicTermStartDate()
		)
		assertEquals(
			LocalDate(2026, 4, 1),
			DEFAULT_EVALUATION_TERM.copy(periodCode = AcademicTermPeriod.APR_JUL).academicTermStartDate()
		)
		assertEquals(
			LocalDate(2026, 7, 1),
			DEFAULT_EVALUATION_TERM.copy(periodCode = AcademicTermPeriod.JUL_AUG).academicTermStartDate()
		)
		assertEquals(
			LocalDate(2026, 9, 1),
			DEFAULT_EVALUATION_TERM.copy(periodCode = AcademicTermPeriod.SEP_DEC).academicTermStartDate()
		)
	}

	@Test
	fun academicWeekStart_whenDateIsMidWeek_returnsMondayOfThatWeek() {
		assertEquals(
			LocalDate(2026, 5, 18),
			LocalDate(2026, 5, 21).academicWeekStart()
		)
	}

	@Test
	fun academicWeekStart_whenDateIsMonday_returnsSameDate() {
		assertEquals(
			LocalDate(2026, 5, 18),
			LocalDate(2026, 5, 18).academicWeekStart()
		)
	}

	@Test
	fun academicWeekStart_whenDateIsSunday_returnsMondayOfSameWeek() {
		assertEquals(
			LocalDate(2026, 5, 18),
			LocalDate(2026, 5, 24).academicWeekStart()
		)
	}

	@Test
	fun computeAcademicWeek_whenDateIsWithinFirstWeek_returnsWeekOne() {
		// April 1st, 2026 is a Wednesday, so the academic week one starts on Monday March 30th.
		assertEquals(
			MIN_ACADEMIC_WEEK,
			computeAcademicWeek(DEFAULT_EVALUATION_TERM, LocalDate(2026, 3, 30))
		)
		assertEquals(
			MIN_ACADEMIC_WEEK,
			computeAcademicWeek(DEFAULT_EVALUATION_TERM, LocalDate(2026, 4, 5))
		)
	}

	@Test
	fun computeAcademicWeek_whenWeekBoundaryIsCrossed_advancesToWeekTwo() {
		assertEquals(
			2,
			computeAcademicWeek(DEFAULT_EVALUATION_TERM, LocalDate(2026, 4, 6))
		)
	}

	@Test
	fun computeAcademicWeek_whenDateIsMidTerm_returnsExpectedWeek() {
		assertEquals(
			8,
			computeAcademicWeek(DEFAULT_EVALUATION_TERM, LocalDate(2026, 5, 21))
		)
	}

	@Test
	fun computeAcademicWeek_whenDateIsBeforeTermStart_clampsToWeekOne() {
		assertEquals(
			MIN_ACADEMIC_WEEK,
			computeAcademicWeek(DEFAULT_EVALUATION_TERM, LocalDate(2026, 1, 1))
		)
	}

	@Test
	fun computeAcademicWeek_whenDateIsOnLastTrackedWeek_returnsWeekTwelve() {
		// Eleven full weeks after Monday March 30th, 2026.
		assertEquals(
			MAX_ACADEMIC_WEEK,
			computeAcademicWeek(DEFAULT_EVALUATION_TERM, LocalDate(2026, 6, 15))
		)
	}

	@Test
	fun computeAcademicWeek_whenDateIsBeyondTrackedWeeks_clampsToWeekTwelve() {
		assertEquals(
			MAX_ACADEMIC_WEEK,
			computeAcademicWeek(DEFAULT_EVALUATION_TERM, LocalDate(2026, 6, 22))
		)
		assertEquals(
			MAX_ACADEMIC_WEEK,
			computeAcademicWeek(DEFAULT_EVALUATION_TERM, LocalDate(2027, 4, 1))
		)
	}
}
