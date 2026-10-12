package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationPoints
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvaluationPointsEngineTest {
	private fun evaluation(
		attemptId: String,
		grade: Double?,
		maxGrade: Double
	) = Evaluation(
		id = "$attemptId-$grade-$maxGrade",
		attemptId = attemptId,
		subjectCode = "MA1111",
		termId = "term",
		scheduleMode = EvaluationScheduleMode.DATED,
		grade = grade,
		maxGrade = maxGrade,
		date = null,
		type = EvaluationType.entries.first(),
		state = EvaluationState.PENDING
	)

	@Test
	fun sumsOnlyTheGradedEvaluationsAndCountsAll() {
		val points = EvaluationPointsEngine.pointsByAttempt(
			listOf(
				evaluation("a", grade = 15.0, maxGrade = 20.0),
				evaluation("a", grade = 8.5, maxGrade = 30.0),
				evaluation("a", grade = null, maxGrade = 50.0)
			)
		)

		assertEquals(
			mapOf("a" to EvaluationPoints(earned = 23.5, evaluated = 50.0, gradedCount = 2, totalCount = 3)),
			points
		)
	}

	@Test
	fun anAttemptWithoutGradesEarnsAndEvaluatesNothing() {
		val points = EvaluationPointsEngine.pointsByAttempt(
			listOf(evaluation("a", grade = null, maxGrade = 20.0), evaluation("a", grade = null, maxGrade = 30.0))
		)

		assertEquals(EvaluationPoints(earned = 0.0, evaluated = 0.0, gradedCount = 0, totalCount = 2), points.getValue("a"))
	}

	@Test
	fun aZeroGradeIsStillGraded() {
		val points = EvaluationPointsEngine.pointsByAttempt(listOf(evaluation("a", grade = 0.0, maxGrade = 20.0)))

		assertEquals(EvaluationPoints(earned = 0.0, evaluated = 20.0, gradedCount = 1, totalCount = 1), points.getValue("a"))
	}

	@Test
	fun keepsEachAttemptApart() {
		val points = EvaluationPointsEngine.pointsByAttempt(
			listOf(evaluation("a", 10.0, 20.0), evaluation("b", 5.0, 10.0), evaluation("b", null, 10.0))
		)

		assertEquals(setOf("a", "b"), points.keys)
		assertEquals(1, points.getValue("a").totalCount)
		assertEquals(2, points.getValue("b").totalCount)
		assertEquals(1, points.getValue("b").gradedCount)
	}

	@Test
	fun noEvaluationsMeansNoEntries() {
		assertTrue(EvaluationPointsEngine.pointsByAttempt(emptyList()).isEmpty())
	}
}
