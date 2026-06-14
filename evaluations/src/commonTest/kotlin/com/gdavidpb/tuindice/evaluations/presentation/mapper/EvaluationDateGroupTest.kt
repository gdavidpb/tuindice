package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.evaluations.presentation.utils.currentEvaluationLocalDate
import com.gdavidpb.tuindice.evaluations.presentation.utils.toEvaluationEpochMillis
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours

class EvaluationDateGroupTest {
	@Test
	fun toEvaluationDateGroup_normalizesExactDatesByLocalDate() {
		val futureDate = currentEvaluationLocalDate().plus(DatePeriod(days = 100))
		val startOfDay = futureDate.toEvaluationEpochMillis()
		val midday = startOfDay + 12.hours.inWholeMilliseconds

		assertEquals(startOfDay.toEvaluationDateGroup(), midday.toEvaluationDateGroup())
	}
}
