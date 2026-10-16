package com.gdavidpb.tuindice.evaluations.domain.mapper

import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvaluationsListedUnderTest {
	private val listed = DEFAULT_PENDING_EVALUATION.copy(
		id = "evaluation-1",
		attemptId = DEFAULT_EVALUATION_SUBJECT.id
	)

	// Its subject is not among the attempts: it was withdrawn, or swapped out, during the corrections.
	private val withdrawn = DEFAULT_PENDING_EVALUATION.copy(
		id = "evaluation-2",
		attemptId = "subject-withdrawn"
	)

	@Test
	fun listedUnder_keepsTheEvaluationsOfTheAttemptsInTheirOrder() {
		val later = listed.copy(id = "evaluation-3")

		val result = listOf(listed, withdrawn, later).listedUnder(attempts = listOf(DEFAULT_EVALUATION_SUBJECT))

		assertEquals(listOf(listed, later), result)
	}

	@Test
	fun listedUnder_leavesOutAnEvaluationWhoseAttemptIsNotThere() {
		val result = listOf(withdrawn).listedUnder(attempts = listOf(DEFAULT_EVALUATION_SUBJECT))

		assertTrue(result.isEmpty())
	}

	@Test
	fun listedUnder_withoutAttempts_listsNothing() {
		val result = listOf(listed).listedUnder(attempts = emptyList())

		assertTrue(result.isEmpty())
	}
}
