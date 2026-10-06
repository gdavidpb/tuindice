package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.presentation.mapper.toShortNameText
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.presentation.utils.toEvaluationEpochMillis
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_TERM
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import kotlinx.datetime.DayOfWeek
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
			continuousLabel = "Continuas",
			currentDate = currentDate
		)

		val currentWeek = items.first { item -> item.key == EvaluationsWeekKey.Academic(8) }
		val nextWeek = items.first { item -> item.key == EvaluationsWeekKey.Academic(9) }

		assertEquals(
			listOf("21"),
			currentWeek.days
				.filter { day -> day.isSelected }
				.map { day -> day.dayText }
		)
		assertTrue(nextWeek.days.any { day -> day.dayText == "28" && day.hasEvaluations })
		assertTrue(nextWeek.days.none { day -> day.isSelected })
	}

	@Test
	fun buildEvaluationsWeekItems_describesEachDayByItsShortWeekdayNameInUpperCase() {
		val items = buildEvaluationsWeekItems(
			currentTerm = DEFAULT_EVALUATION_TERM,
			evaluations = emptyList(),
			weekLabelPattern = "Semana %1${'$'}d",
			continuousLabel = "Continuas",
			currentDate = LocalDate(2026, 5, 21)
		)

		// Every week runs from Monday to Sunday; the name is read where the strip is drawn.
		items.forEach { item ->
			assertEquals(
				DayOfWeek.entries.map { day -> UiText.Uppercase(day.toShortNameText()) },
				item.days.map { day -> day.weekdayText }
			)
		}
	}

	@Test
	fun buildEvaluationsWeekItems_addsContinuousItemBeforeWeekOne_whenContinuousEvaluationsExist() {
		val currentDate = LocalDate(2026, 5, 21)

		val items = buildEvaluationsWeekItems(
			currentTerm = DEFAULT_EVALUATION_TERM,
			evaluations = listOf(
				DEFAULT_PENDING_EVALUATION.copy(
					scheduleMode = EvaluationScheduleMode.CONTINUOUS,
					date = currentDate.toEvaluationEpochMillis()
				)
			),
			weekLabelPattern = "Semana %1${'$'}d",
			continuousLabel = "Continuas",
			currentDate = currentDate
		)

		assertEquals(EvaluationsWeekKey.Continuous, items.first().key)
		assertEquals("Continuas", items.first().labelText)
		assertTrue(items.first().days.isEmpty())
		assertEquals(EvaluationsWeekKey.Academic(1), items[1].key)
		assertTrue(items.none { item ->
			item.key is EvaluationsWeekKey.Academic &&
				item.days.any { day -> day.hasEvaluations }
		})
	}
}
