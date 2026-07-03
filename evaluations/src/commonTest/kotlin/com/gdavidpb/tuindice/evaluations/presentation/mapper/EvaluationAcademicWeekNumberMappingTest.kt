package com.gdavidpb.tuindice.evaluations.presentation.mapper

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
}
