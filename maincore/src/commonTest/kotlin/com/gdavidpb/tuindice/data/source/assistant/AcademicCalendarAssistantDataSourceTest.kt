package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicCalendarAssistantDataSourceTest {
	@Test
	fun calendarOf_opensWeekOneOnTheClassesStart_andCountsTheWeeksOfTheTerm() {
		val calendar = calendarOf(termOf(classesStartDate = LocalDate(2026, 1, 12)))

		assertEquals(LocalDate(2026, 1, 12), calendar.weekOneStart)
		assertEquals(1..12, calendar.weeks)
		assertEquals(2, calendar.weekOf(LocalDate(2026, 1, 21)))
		assertEquals(12, calendar.weekOf(LocalDate(2026, 3, 30)))
	}

	@Test
	fun calendarOf_clampsADateOutsideTheTermToItsFirstOrLastWeek() {
		val calendar = calendarOf(termOf(classesStartDate = LocalDate(2026, 1, 12)))

		assertEquals(1, calendar.weekOf(LocalDate(2025, 12, 1)))
		assertEquals(12, calendar.weekOf(LocalDate(2026, 4, 6)))
	}

	@Test
	fun calendarOf_withoutAClassesStart_opensOnTheMondayOfTheFirstDayOfTheStartMonth() {
		val calendar = calendarOf(termOf(classesStartDate = null))

		assertEquals(LocalDate(2025, 12, 29), calendar.weekOneStart)
		assertEquals(2, calendar.weekOf(LocalDate(2026, 1, 5)))
	}

	private fun termOf(classesStartDate: LocalDate?) = EvaluationTermDescriptor(
		id = "term",
		periodYear = 2026,
		periodCode = AcademicTermPeriod.JAN_MAR,
		periodLabel = "2026-1",
		classesStartDate = classesStartDate
	)
}
