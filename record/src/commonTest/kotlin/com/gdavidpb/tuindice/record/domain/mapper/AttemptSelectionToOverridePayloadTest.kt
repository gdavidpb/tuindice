package com.gdavidpb.tuindice.record.domain.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AttemptSelectionToOverridePayloadTest {
	@Test
	fun `normal status keeps numeric score and pending outcome`() {
		val (score, outcome) = attemptSelectionToOverridePayload(
			grade = 4,
			status = SubjectStatus.NORMAL
		)

		assertEquals(AttemptScore.numeric(4), score)
		assertEquals(AttemptOutcome.PENDING, outcome)
	}

	@Test
	fun `without effect does not force an outcome`() {
		val (score, outcome) = attemptSelectionToOverridePayload(
			grade = 2,
			status = SubjectStatus.WITHOUT_EFFECT
		)

		assertEquals(AttemptScore.numeric(2), score)
		assertNull(outcome)
	}
}
