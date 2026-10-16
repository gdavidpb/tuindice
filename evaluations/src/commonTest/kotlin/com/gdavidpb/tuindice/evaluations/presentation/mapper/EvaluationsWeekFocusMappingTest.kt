package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EvaluationsWeekFocusMappingTest {
	// Week 1 opens on the Monday 2026-09-07, so week 2 opens on 2026-09-14.
	private val weekOneStart = LocalDate(2026, 9, 7)

	@Test
	fun evaluationsWeekKeyContaining_isWeekOneForADateBeforeTheFirstWeek() {
		val key = evaluationsWeekKeyContaining(weekOneStart = weekOneStart, date = LocalDate(2026, 8, 31))

		assertEquals(EvaluationsWeekKey.Academic(weekNumber = AcademicWeekEngine.MIN_ACADEMIC_WEEK), key)
	}

	@Test
	fun evaluationsWeekKeyContaining_isTheWeekTheDateFallsIn() {
		val key = evaluationsWeekKeyContaining(weekOneStart = weekOneStart, date = LocalDate(2026, 9, 16))

		assertEquals(EvaluationsWeekKey.Academic(weekNumber = 2), key)
	}

	@Test
	fun evaluationsWeekKeyContaining_isTheLastWeekForADateAfterTheLastWeek() {
		val key = evaluationsWeekKeyContaining(weekOneStart = weekOneStart, date = LocalDate(2027, 6, 1))

		assertEquals(EvaluationsWeekKey.Academic(weekNumber = AcademicWeekEngine.MAX_ACADEMIC_WEEK), key)
	}

	@Test
	fun evaluationsWeekKeyContaining_isNullWithoutAWeekOneStart() {
		assertNull(evaluationsWeekKeyContaining(weekOneStart = null, date = LocalDate(2026, 9, 16)))
	}
}
