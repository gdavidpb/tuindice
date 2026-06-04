package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.presentation.mapper.localizedShortWeekdayNames
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.extension.currentEvaluationLocalDate
import com.gdavidpb.tuindice.evaluations.presentation.extension.toEvaluationLocalDate
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationWeekDayItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

fun buildEvaluationsWeekItems(
	currentTerm: EvaluationTermDescriptor?,
	evaluations: List<Evaluation>,
	weekLabelPattern: String,
	currentDate: LocalDate = currentEvaluationLocalDate()
): List<EvaluationsWeekItem> {
	val termStart = currentTerm?.academicTermStartDate() ?: currentDate
	val evaluationDates = evaluations.mapNotNull { evaluation ->
		evaluation.date?.toEvaluationLocalDate()
	}.toSet()
	val weekdayNames = localizedShortWeekdayNames()

	return (MIN_ACADEMIC_WEEK..MAX_ACADEMIC_WEEK).map { weekNumber ->
		val weekStart = termStart
			.plus(DatePeriod(days = (weekNumber - 1) * DAYS_PER_WEEK))
			.academicWeekStart()

		EvaluationsWeekItem(
			weekNumber = weekNumber,
			labelText = weekLabelPattern.replace("%1${'$'}d", weekNumber.toString()),
			days = (0..6).map { offset ->
				val date = weekStart.plus(DatePeriod(days = offset))
				EvaluationWeekDayItem(
					weekdayText = weekdayNames[date.dayOfWeek.ordinal].uppercase(),
					dayText = date.day.toString(),
					isSelected = date == currentDate,
					hasEvaluations = date in evaluationDates
				)
			}
		)
	}
}
