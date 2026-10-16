package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class EvaluationStateEngineTest {
	private val today = LocalDate(2026, 6, 10)

	private fun stateOf(
		mode: EvaluationScheduleMode = EvaluationScheduleMode.DATED,
		hasGrade: Boolean = false,
		date: LocalDate? = null
	) = EvaluationStateEngine.stateOf(mode, hasGrade, date, today)

	@Test
	fun continuousIsAlwaysContinuous() {
		assertEquals(EvaluationState.CONTINUOUS, stateOf(EvaluationScheduleMode.CONTINUOUS))
		assertEquals(
			EvaluationState.CONTINUOUS,
			stateOf(EvaluationScheduleMode.CONTINUOUS, hasGrade = true, date = LocalDate(2020, 1, 1))
		)
	}

	@Test
	fun aGradedEvaluationIsCompletedWhateverItsDate() {
		assertEquals(EvaluationState.COMPLETED, stateOf(hasGrade = true))
		assertEquals(EvaluationState.COMPLETED, stateOf(hasGrade = true, date = LocalDate(2026, 1, 1)))
	}

	@Test
	fun anUngradedEvaluationWithAPastDateIsOverdue() {
		assertEquals(EvaluationState.OVERDUE, stateOf(date = LocalDate(2026, 6, 9)))
	}

	@Test
	fun anUngradedEvaluationDueTodayOrLaterIsPending() {
		assertEquals(EvaluationState.PENDING, stateOf(date = today))
		assertEquals(EvaluationState.PENDING, stateOf(date = LocalDate(2026, 6, 11)))
	}

	@Test
	fun anUngradedEvaluationWithoutDateIsPending() {
		assertEquals(EvaluationState.PENDING, stateOf())
	}
}
