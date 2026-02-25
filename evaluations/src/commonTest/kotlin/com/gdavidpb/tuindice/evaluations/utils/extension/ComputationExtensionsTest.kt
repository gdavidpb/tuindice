package com.gdavidpb.tuindice.evaluations.utils.extension

import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals

class ComputationExtensionsTest {
	@Test
	fun toSubjectGrade_mapsBoundariesToExpectedGradeBuckets() {
		assertEquals(1, 0.0.toSubjectGrade())
		assertEquals(1, 29.0.toSubjectGrade())
		assertEquals(2, 30.0.toSubjectGrade())
		assertEquals(2, 49.0.toSubjectGrade())
		assertEquals(3, 50.0.toSubjectGrade())
		assertEquals(3, 69.0.toSubjectGrade())
		assertEquals(4, 70.0.toSubjectGrade())
		assertEquals(4, 84.0.toSubjectGrade())
		assertEquals(5, 85.0.toSubjectGrade())
		assertEquals(5, 100.0.toSubjectGrade())
	}

	@Test
	fun computeEvaluationState_resolvesStateByDateAndGradeRules() {
		val now = currentTimeMillis()

		assertEquals(EvaluationState.CONTINUOUS, computeEvaluationState(grade = null, date = null))
		assertEquals(EvaluationState.OVERDUE, computeEvaluationState(grade = null, date = now - 1_000))
		assertEquals(EvaluationState.COMPLETED, computeEvaluationState(grade = 4.0, date = now - 1_000))
		assertEquals(EvaluationState.PENDING, computeEvaluationState(grade = null, date = now + 1_000))
	}
}
