package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.summary.testing.statisticsTerm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SummaryStatisticsTermLabelsTest {
	@Test
	fun when_noTermIsGiven_then_thereAreNoAxisLabels() {
		assertTrue(axisLabelsOf(emptyList()).isEmpty())
	}

	@Test
	fun when_termsAreDistinct_then_EachAxisLabelIsItsInitialsAndTheLastTwoDigitsOfItsYear() {
		val first = statisticsTerm(year = 2023, periodCode = AcademicTermPeriod.SEP_DEC)
		val second = statisticsTerm(year = 2024, periodCode = AcademicTermPeriod.JAN_MAR)

		val labels = axisLabelsOf(listOf(first, second))

		assertEquals("S-D 23", labels.getValue(first.termKey))
		assertEquals("E-M 24", labels.getValue(second.termKey))
	}

	@Test
	fun when_twoTermsWouldBeNamedAlike_then_bothKeepTheirThreeLetterMonths() {
		val earlier = statisticsTerm(year = 2024, periodCode = AcademicTermPeriod.JAN_MAR)
		val later = statisticsTerm(year = 2124, periodCode = AcademicTermPeriod.JAN_MAR)

		val labels = axisLabelsOf(listOf(earlier, later))

		assertEquals("Ene-Mar 24", labels.getValue(earlier.termKey))
		assertEquals("Ene-Mar 24", labels.getValue(later.termKey))
	}

	@Test
	fun when_aTermIsWrittenInFull_then_ItsPeriodLeadsItsYear() {
		val term = statisticsTerm(year = 2026, periodCode = AcademicTermPeriod.SEP_DEC)

		assertEquals("${AcademicTermPeriod.SEP_DEC.shortLabel} 2026", term.fullLabel())
	}
}
