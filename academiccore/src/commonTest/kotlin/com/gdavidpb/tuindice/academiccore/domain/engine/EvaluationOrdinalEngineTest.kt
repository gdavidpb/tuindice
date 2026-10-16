package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import kotlin.test.Test
import kotlin.test.assertEquals

class EvaluationOrdinalEngineTest {
	private fun evaluation(
		id: String,
		date: Long?,
		attemptId: String = "attempt-a",
		type: EvaluationType = EvaluationType.TEST
	) = Evaluation(
		id = id,
		attemptId = attemptId,
		subjectCode = "CI5437",
		termId = "term",
		scheduleMode = EvaluationScheduleMode.DATED,
		grade = null,
		maxGrade = 20.0,
		date = date,
		type = type,
		state = EvaluationState.PENDING
	)

	@Test
	fun ordinalsById_countsByDate_whateverTheOrderGiven() {
		val ordinals = EvaluationOrdinalEngine.ordinalsById(
			listOf(evaluation("late", date = 30), evaluation("early", date = 10), evaluation("middle", date = 20))
		)

		assertEquals(mapOf("early" to 1, "middle" to 2, "late" to 3), ordinals)
	}

	@Test
	fun ordinalsById_countsEachAttemptAndEachTypeApart() {
		val ordinals = EvaluationOrdinalEngine.ordinalsById(
			listOf(
				evaluation("a-test-1", date = 10),
				evaluation("a-essay-1", date = 11, type = EvaluationType.ESSAY),
				evaluation("b-test-1", date = 12, attemptId = "attempt-b"),
				evaluation("a-test-2", date = 13)
			)
		)

		assertEquals(
			mapOf("a-test-1" to 1, "a-essay-1" to 1, "b-test-1" to 1, "a-test-2" to 2),
			ordinals
		)
	}

	@Test
	fun ordinalsById_putsTheOnesWithNoDateFirst_andKeepsTheOrderGivenOnTies() {
		val ordinals = EvaluationOrdinalEngine.ordinalsById(
			listOf(
				evaluation("dated", date = 10),
				evaluation("undated", date = null),
				evaluation("tie-1", date = 20),
				evaluation("tie-2", date = 20)
			)
		)

		assertEquals(mapOf("undated" to 1, "dated" to 2, "tie-1" to 3, "tie-2" to 4), ordinals)
	}

	@Test
	fun ordinalsById_ofNoEvaluations_isEmpty() {
		assertEquals(emptyMap(), EvaluationOrdinalEngine.ordinalsById(emptyList()))
	}
}
