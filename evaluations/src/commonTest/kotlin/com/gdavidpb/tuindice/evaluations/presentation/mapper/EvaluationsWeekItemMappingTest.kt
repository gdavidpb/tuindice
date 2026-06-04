package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.evaluations.presentation.extension.toEvaluationEpochMillis
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_TERM
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvaluationsWeekItemMappingTest {
	@Test
	fun buildEvaluationsWeekItems_marksOnlyTodayAsSelected() {
		val currentDate = LocalDate(2026, 5, 21)
		val nextWeekEvaluationDate = LocalDate(2026, 5, 28)

		val items = buildEvaluationsWeekItems(
			currentTerm = DEFAULT_EVALUATION_TERM,
			evaluations = listOf(
				DEFAULT_PENDING_EVALUATION.copy(
					date = nextWeekEvaluationDate.toEvaluationEpochMillis()
				)
			),
			weekLabelPattern = "Semana %1${'$'}d",
			currentDate = currentDate
		)

		val currentWeek = items.first { item -> item.weekNumber == 8 }
		val nextWeek = items.first { item -> item.weekNumber == 9 }

		assertEquals(
			listOf("21"),
			currentWeek.days
				.filter { day -> day.isSelected }
				.map { day -> day.dayText }
		)
		assertTrue(nextWeek.days.any { day -> day.dayText == "28" && day.hasEvaluations })
		assertTrue(nextWeek.days.none { day -> day.isSelected })
	}
}
