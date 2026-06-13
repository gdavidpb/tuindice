package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_TERM
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultEvaluationsWeekNumberMappingTest {
	@Test
	fun defaultEvaluationsWeekNumber_whenTermIsKnown_returnsAcademicWeekOfCurrentDate() {
		assertEquals(
			8,
			defaultEvaluationsWeekNumber(
				currentTerm = DEFAULT_EVALUATION_TERM,
				currentDate = LocalDate(2026, 5, 21)
			)
		)
	}

	@Test
	fun defaultEvaluationsWeekNumber_whenTermIsUnknown_fallsBackToWeekOne() {
		assertEquals(
			MIN_ACADEMIC_WEEK,
			defaultEvaluationsWeekNumber(
				currentTerm = null,
				currentDate = LocalDate(2026, 5, 21)
			)
		)
	}

	@Test
	fun defaultEvaluationsWeekNumber_whenCurrentDatePrecedesTerm_clampsToWeekOne() {
		assertEquals(
			MIN_ACADEMIC_WEEK,
			defaultEvaluationsWeekNumber(
				currentTerm = DEFAULT_EVALUATION_TERM,
				currentDate = LocalDate(2026, 2, 1)
			)
		)
	}

	@Test
	fun defaultEvaluationsWeekNumber_whenCurrentDateExceedsTerm_clampsToWeekTwelve() {
		assertEquals(
			MAX_ACADEMIC_WEEK,
			defaultEvaluationsWeekNumber(
				currentTerm = DEFAULT_EVALUATION_TERM,
				currentDate = LocalDate(2026, 12, 31)
			)
		)
	}
}
