package com.gdavidpb.tuindice.record.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AttemptSelectionToOverridePayloadTest {
	@Test
	fun `null outcome keeps numeric score without forcing an override outcome`() {
		val (score, outcome) = attemptSelectionToOverridePayload(
			grade = 4,
			outcome = null
		)

		assertEquals(AttemptScore.numeric(4), score)
		assertNull(outcome)
	}

	@Test
	fun `explicit outcome is preserved`() {
		val (score, outcome) = attemptSelectionToOverridePayload(
			grade = 2,
			outcome = AttemptOutcome.RETIRED
		)

		assertEquals(AttemptScore.numeric(2), score)
		assertEquals(AttemptOutcome.RETIRED, outcome)
	}
}
