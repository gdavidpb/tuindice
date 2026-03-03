package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateFilter
import com.gdavidpb.tuindice.evaluations.presentation.extension.currentEvaluationLocalDate
import com.gdavidpb.tuindice.evaluations.presentation.extension.toEvaluationEpochMillis
import com.gdavidpb.tuindice.evaluations.utils.extension.computeAvailableFilters
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

class EvaluationDateGroupTest {
	@Test
	fun toEvaluationDateGroup_normalizesExactDatesByLocalDate() {
		val futureDate = currentEvaluationLocalDate().plus(DatePeriod(days = 100))
		val startOfDay = futureDate.toEvaluationEpochMillis()
		val midday = startOfDay + 12.hours.inWholeMilliseconds

		assertEquals(startOfDay.toEvaluationDateGroup(), midday.toEvaluationDateGroup())
	}

	@Test
	fun computeAvailableFilters_createsSingleDateFilterPerLocalDate() {
		val futureDate = currentEvaluationLocalDate().plus(DatePeriod(days = 100))
		val startOfDay = futureDate.toEvaluationEpochMillis()
		val midday = startOfDay + 12.hours.inWholeMilliseconds
		val evaluations = listOf(
			evaluation(id = "evaluation-1", date = startOfDay),
			evaluation(id = "evaluation-2", date = midday)
		)

		val filters = evaluations.computeAvailableFilters(
			pendingLabel = "Pendientes",
			completedLabel = "Completadas",
			noGradeLabel = "Sin nota",
			dateTextMapping = EvaluationDateTextMapping(
				noDateLabel = "Evaluacion continua",
				todayLabel = "Hoy",
				tomorrowLabel = "Manana",
				yesterdayLabel = "Ayer",
				pastWeekPattern = "El %1\$s",
				thisWeekPattern = "Este %1\$s",
				nextWeekPattern = "El proximo %1\$s",
				weeksAheadPattern = "En %1\$d semanas"
			)
		)

		assertEquals(1, filters.filterIsInstance<EvaluationDateFilter>().size)
		assertTrue(filters.filterIsInstance<EvaluationDateFilter>().single().match(evaluations.first()))
		assertTrue(filters.filterIsInstance<EvaluationDateFilter>().single().match(evaluations.last()))
	}

	private fun evaluation(
		id: String,
		date: Long
	) = Evaluation(
		id = id,
		subjectId = "subject-1",
		subjectCode = "INF-101",
		quarterId = "quarter-1",
		grade = null,
		maxGrade = 100.0,
		date = date,
		type = EvaluationType.QUIZ,
		state = EvaluationState.PENDING
	)
}
