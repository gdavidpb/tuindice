package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_COMPLETED_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_TERM
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultEvaluationsWeekKeyMappingTest {
	@Test
	fun defaultEvaluationsWeekKey_whenDatedEvaluationsExist_selectsAcademicWeekOfCurrentDate() {
		val continuousEvaluation = DEFAULT_COMPLETED_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.CONTINUOUS
		)

		assertEquals(
			EvaluationsWeekKey.Academic(8),
			defaultEvaluationsWeekKey(
				currentTerm = DEFAULT_EVALUATION_TERM,
				evaluations = listOf(continuousEvaluation, DEFAULT_PENDING_EVALUATION),
				currentDate = LocalDate(2026, 5, 21)
			)
		)
	}

	@Test
	fun defaultEvaluationsWeekKey_whenAllEvaluationsAreContinuous_selectsContinuousKey() {
		assertEquals(
			EvaluationsWeekKey.Continuous,
			defaultEvaluationsWeekKey(
				currentTerm = DEFAULT_EVALUATION_TERM,
				evaluations = listOf(
					DEFAULT_PENDING_EVALUATION.copy(scheduleMode = EvaluationScheduleMode.CONTINUOUS),
					DEFAULT_COMPLETED_EVALUATION.copy(scheduleMode = EvaluationScheduleMode.CONTINUOUS)
				),
				currentDate = LocalDate(2026, 5, 21)
			)
		)
	}

	@Test
	fun defaultEvaluationsWeekKey_whenThereAreNoEvaluations_selectsContinuousKey() {
		assertEquals(
			EvaluationsWeekKey.Continuous,
			defaultEvaluationsWeekKey(
				currentTerm = DEFAULT_EVALUATION_TERM,
				evaluations = emptyList(),
				currentDate = LocalDate(2026, 5, 21)
			)
		)
	}

	@Test
	fun defaultEvaluationsWeekKey_whenTermIsUnknown_fallsBackToAcademicWeekOne() {
		assertEquals(
			EvaluationsWeekKey.Academic(MIN_ACADEMIC_WEEK),
			defaultEvaluationsWeekKey(
				currentTerm = null,
				evaluations = listOf(DEFAULT_PENDING_EVALUATION),
				currentDate = LocalDate(2026, 5, 21)
			)
		)
	}
}
