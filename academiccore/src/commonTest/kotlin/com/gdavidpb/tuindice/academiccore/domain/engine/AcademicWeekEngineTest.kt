package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AcademicWeekEngineTest {
	// April 2026 starts on a Wednesday: week 1 opens on Monday March 30.
	private val weekOne = LocalDate(2026, 3, 30)

	@Test
	fun weekOneStart_withoutClassesStartDate_isTheMondayOfThePeriodStartMonthsFirstDay() {
		assertEquals(
			LocalDate(2026, 3, 30),
			AcademicWeekEngine.weekOneStart(2026, AcademicTermPeriod.APR_JUL)
		)
		assertEquals(
			LocalDate(2025, 12, 29),
			AcademicWeekEngine.weekOneStart(2026, AcademicTermPeriod.JAN_MAR)
		)
		assertEquals(
			LocalDate(2026, 8, 31),
			AcademicWeekEngine.weekOneStart(2026, AcademicTermPeriod.SEP_DEC)
		)
	}

	@Test
	fun weekOneStart_withAPlausibleClassesStartDate_isTheMondayOfItsWeek() {
		// Thursday September 17th.
		assertEquals(
			LocalDate(2026, 9, 14),
			AcademicWeekEngine.weekOneStart(2026, AcademicTermPeriod.SEP_DEC, LocalDate(2026, 9, 17))
		)
	}

	@Test
	fun weekOneStart_withAClassesStartDateOnAMonday_isThatSameDate() {
		// Monday September 28th.
		assertEquals(
			LocalDate(2026, 9, 28),
			AcademicWeekEngine.weekOneStart(2026, AcademicTermPeriod.SEP_DEC, LocalDate(2026, 9, 28))
		)
	}

	@Test
	fun weekOneStart_withAClassesStartDateOnASunday_isTheMondaySixDaysBefore() {
		// Sunday September 27th belongs to the week that opened on Monday the 21st.
		assertEquals(
			LocalDate(2026, 9, 21),
			AcademicWeekEngine.weekOneStart(2026, AcademicTermPeriod.SEP_DEC, LocalDate(2026, 9, 27))
		)
	}

	@Test
	fun weekOneStart_withAnImplausibleClassesStartDate_fallsBackToThePeriodStart() {
		assertEquals(
			LocalDate(2026, 8, 31),
			AcademicWeekEngine.weekOneStart(2026, AcademicTermPeriod.SEP_DEC, LocalDate(2027, 1, 12))
		)
	}

	@Test
	fun isPlausibleClassesStartDate_inSeptember_spansAugustFirstToOctoberThirtyFirst() {
		val period = AcademicTermPeriod.SEP_DEC

		assertFalse(AcademicWeekEngine.isPlausibleClassesStartDate(2026, period, LocalDate(2026, 7, 31)))
		assertTrue(AcademicWeekEngine.isPlausibleClassesStartDate(2026, period, LocalDate(2026, 8, 1)))
		assertTrue(AcademicWeekEngine.isPlausibleClassesStartDate(2026, period, LocalDate(2026, 9, 15)))
		assertTrue(AcademicWeekEngine.isPlausibleClassesStartDate(2026, period, LocalDate(2026, 10, 31)))
		assertFalse(AcademicWeekEngine.isPlausibleClassesStartDate(2026, period, LocalDate(2026, 11, 1)))
	}

	@Test
	fun isPlausibleClassesStartDate_inJanuary_startsInDecemberOfThePreviousYear() {
		val period = AcademicTermPeriod.JAN_MAR

		assertFalse(AcademicWeekEngine.isPlausibleClassesStartDate(2026, period, LocalDate(2025, 11, 30)))
		assertTrue(AcademicWeekEngine.isPlausibleClassesStartDate(2026, period, LocalDate(2025, 12, 1)))
		assertTrue(AcademicWeekEngine.isPlausibleClassesStartDate(2026, period, LocalDate(2026, 1, 12)))
		assertTrue(AcademicWeekEngine.isPlausibleClassesStartDate(2026, period, LocalDate(2026, 2, 28)))
		assertFalse(AcademicWeekEngine.isPlausibleClassesStartDate(2026, period, LocalDate(2026, 3, 1)))
		// The same December of the period's own year is a year too late.
		assertFalse(AcademicWeekEngine.isPlausibleClassesStartDate(2026, period, LocalDate(2026, 12, 1)))
	}

	@Test
	fun isPlausibleClassesStartDate_inALeapYear_endsOnFebruaryTwentyNinth() {
		val period = AcademicTermPeriod.JAN_MAR

		assertTrue(AcademicWeekEngine.isPlausibleClassesStartDate(2028, period, LocalDate(2028, 2, 29)))
		assertFalse(AcademicWeekEngine.isPlausibleClassesStartDate(2028, period, LocalDate(2028, 3, 1)))
	}

	@Test
	fun weekOf_countsWeeksFromOneAndClampsToTheTrackedRange() {
		assertEquals(1, AcademicWeekEngine.weekOf(weekOne, LocalDate(2026, 3, 30)))
		assertEquals(1, AcademicWeekEngine.weekOf(weekOne, LocalDate(2026, 4, 5)))
		assertEquals(2, AcademicWeekEngine.weekOf(weekOne, LocalDate(2026, 4, 6)))
		assertEquals(12, AcademicWeekEngine.weekOf(weekOne, LocalDate(2026, 6, 15)))
		assertEquals(1, AcademicWeekEngine.weekOf(weekOne, LocalDate(2026, 1, 1)))
		assertEquals(12, AcademicWeekEngine.weekOf(weekOne, LocalDate(2026, 6, 22)))
		assertEquals(12, AcademicWeekEngine.weekOf(weekOne, LocalDate(2027, 4, 1)))
	}

	@Test
	fun weekOrNull_isNullOutsideTheTrackedRange() {
		assertEquals(1, AcademicWeekEngine.weekOrNull(weekOne, LocalDate(2026, 3, 30)))
		assertEquals(12, AcademicWeekEngine.weekOrNull(weekOne, LocalDate(2026, 6, 21)))
		assertNull(AcademicWeekEngine.weekOrNull(weekOne, LocalDate(2026, 6, 22)))
	}

	@Test
	fun weekOrNull_roundsDownBeforeTheStart() {
		// One day before week 1 is week 0, not week 1 as truncating the division would say.
		assertNull(AcademicWeekEngine.weekOrNull(weekOne, LocalDate(2026, 3, 29)))
		assertNull(AcademicWeekEngine.weekOrNull(weekOne, LocalDate(2026, 3, 23)))
		assertNull(AcademicWeekEngine.weekOrNull(weekOne, LocalDate(2026, 1, 1)))
	}
}
