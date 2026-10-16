package com.gdavidpb.tuindice.evaluations.presentation.contract

import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationsNoAttemptsReason
import com.gdavidpb.tuindice.evaluations.presentation.mapper.resolveNoAttemptsExplanation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.resolveRecordDataUnavailableExplanation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Where the bar offers the class schedule: wherever the term has one and the screen is not saying
 * that there is no term or no record to speak of.
 */
class EvaluationsStateTopBarTest {
	@Test
	fun empty_offersTheScheduleOnlyWhenTheTermHasOne() {
		assertEquals(TopBarConfig.EvaluationsWithSchedule, Evaluations.State.Empty(hasSchedule = true).topBarConfig)
		assertNull(Evaluations.State.Empty(hasSchedule = false).topBarConfig)
		assertNull(Evaluations.State.Empty().topBarConfig)
	}

	@Test
	fun theStatesWithoutATermOrARecord_neverOfferTheSchedule() {
		assertNull(
			Evaluations.State.NoAttempts(
				explanation = resolveNoAttemptsExplanation(EvaluationsNoAttemptsReason.NoCurrentTerm)
			).topBarConfig
		)
		assertNull(
			Evaluations.State.RecordDataUnavailable(
				explanation = resolveRecordDataUnavailableExplanation(isNewStudentNoRecord = false)
			).topBarConfig
		)
	}
}
