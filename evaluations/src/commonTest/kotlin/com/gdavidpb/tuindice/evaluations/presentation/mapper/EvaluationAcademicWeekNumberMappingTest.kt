package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine.MAX_ACADEMIC_WEEK
import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine.MIN_ACADEMIC_WEEK
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.evaluations.presentation.utils.toEvaluationEpochMillis
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_TERM
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EvaluationAcademicWeekNumberMappingTest {
	@Test
	fun academicWeekNumber_whenEvaluationIsContinuous_returnsNull() {
		val evaluation = DEFAULT_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.CONTINUOUS,
			date = LocalDate(2026, 5, 28).toEvaluationEpochMillis()
		)

		assertNull(evaluation.academicWeekNumber(currentTerm = DEFAULT_EVALUATION_TERM))
	}

	@Test
	fun academicWeekNumber_whenDatedEvaluationHasNoDate_returnsNull() {
		val evaluation = DEFAULT_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.DATED,
			date = null
		)

		assertNull(evaluation.academicWeekNumber(currentTerm = DEFAULT_EVALUATION_TERM))
	}

	@Test
	fun academicWeekNumber_whenDatedEvaluationHasDateAndTerm_returnsWeekOfEvaluationDate() {
		val evaluation = DEFAULT_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.DATED,
			date = LocalDate(2026, 5, 28).toEvaluationEpochMillis()
		)

		assertEquals(9, evaluation.academicWeekNumber(currentTerm = DEFAULT_EVALUATION_TERM))
	}

	@Test
	fun academicWeekNumber_whenTermIsUnknown_fallsBackToWeekOne() {
		val evaluation = DEFAULT_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.DATED,
			date = LocalDate(2026, 5, 28).toEvaluationEpochMillis()
		)

		assertEquals(MIN_ACADEMIC_WEEK, evaluation.academicWeekNumber(currentTerm = null))
	}

	// SEP_DEC 2026 with classes starting on Monday September 28th: week 1 runs from the 28th to
	// October 4th and week 12 ends on December 20th.
	private val termWithClassesStart = DEFAULT_EVALUATION_TERM.copy(
		periodCode = AcademicTermPeriod.SEP_DEC,
		classesStartDate = LocalDate(2026, 9, 28)
	)

	private fun weekOf(date: LocalDate) = DEFAULT_PENDING_EVALUATION
		.copy(scheduleMode = EvaluationScheduleMode.DATED, date = date.toEvaluationEpochMillis())
		.academicWeekNumber(currentTerm = termWithClassesStart)

	// The behavior the tab has always had, kept on purpose: an evaluation outside the twelve weeks is
	// listed under the nearest one even though the strip has no such day there.
	@Test
	fun academicWeekNumber_withARealStart_countsFromItsMonday() {
		assertEquals(1, weekOf(LocalDate(2026, 9, 28)))
		assertEquals(3, weekOf(LocalDate(2026, 10, 15)))
		assertEquals(12, weekOf(LocalDate(2026, 12, 20)))
	}

	@Test
	fun academicWeekNumber_withARealStart_holdsAnEvaluationBeforeWeekOneInWeekOne() {
		assertEquals(MIN_ACADEMIC_WEEK, weekOf(LocalDate(2026, 9, 27)))
		assertEquals(MIN_ACADEMIC_WEEK, weekOf(LocalDate(2026, 8, 1)))
	}

	@Test
	fun academicWeekNumber_withARealStart_holdsAnEvaluationAfterWeekTwelveInWeekTwelve() {
		assertEquals(MAX_ACADEMIC_WEEK, weekOf(LocalDate(2026, 12, 21)))
		assertEquals(MAX_ACADEMIC_WEEK, weekOf(LocalDate(2027, 1, 20)))
	}
}
