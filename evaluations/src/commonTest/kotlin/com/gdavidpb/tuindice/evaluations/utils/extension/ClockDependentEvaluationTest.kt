@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.evaluations.utils.extension

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationState
import com.gdavidpb.tuindice.evaluations.presentation.utils.isDateInPast
import com.gdavidpb.tuindice.evaluations.testing.fixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** What is overdue or in the past is told by the clock the caller hands over, never by the day it runs. */
class ClockDependentEvaluationTest {
	private val evaluationDate = Instant.parse("2026-06-10T16:00:00Z").toEpochMilliseconds()

	@Test
	fun computeEvaluationState_followsTheClockItIsGiven() {
		fun stateAt(iso: String) = computeEvaluationState(
			scheduleMode = EvaluationScheduleMode.DATED,
			grade = null,
			date = evaluationDate,
			clock = fixedClock(iso)
		)

		assertEquals(EvaluationState.PENDING, stateAt("2026-06-09T12:00:00Z"))
		assertEquals(EvaluationState.OVERDUE, stateAt("2026-10-15T12:00:00Z"))
	}

	@Test
	fun isDateInPast_followsTheClockItIsGiven() {
		assertFalse(evaluationDate.isDateInPast(fixedClock("2026-06-09T12:00:00Z")))
		assertTrue(evaluationDate.isDateInPast(fixedClock("2026-10-15T12:00:00Z")))
		assertFalse((null as Long?).isDateInPast(fixedClock("2026-10-15T12:00:00Z")))
	}
}
