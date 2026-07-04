package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.presentation.mapper.localizedShortWeekdayNames
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationWeekDayItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.presentation.utils.currentEvaluationLocalDate
import com.gdavidpb.tuindice.evaluations.presentation.utils.toEvaluationLocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

fun buildEvaluationsWeekItems(
	currentTerm: EvaluationTermDescriptor?,
	evaluations: List<Evaluation>,
	weekLabelPattern: String,
	continuousLabel: String,
	currentDate: LocalDate = currentEvaluationLocalDate()
): List<EvaluationsWeekItem> {
	val termStart = currentTerm?.academicTermStartDate() ?: currentDate
	val evaluationDates = evaluations.mapNotNull { evaluation ->
		if (evaluation.scheduleMode == EvaluationScheduleMode.CONTINUOUS) {
			null
		} else {
			evaluation.date?.toEvaluationLocalDate()
		}
	}.toSet()
	val weekdayNames = localizedShortWeekdayNames()
	val continuousItem = if (evaluations.any { evaluation -> evaluation.scheduleMode == EvaluationScheduleMode.CONTINUOUS }) {
		listOf(
			EvaluationsWeekItem(
				key = EvaluationsWeekKey.Continuous,
				labelText = continuousLabel,
				days = emptyList(),
				isCurrent = false
			)
		)
	} else {
		emptyList()
	}

	val weeklyItems = (MIN_ACADEMIC_WEEK..MAX_ACADEMIC_WEEK).map { weekNumber ->
		val weekStart = termStart
			.plus(DatePeriod(days = (weekNumber - 1) * DAYS_PER_WEEK))
			.academicWeekStart()

		EvaluationsWeekItem(
			key = EvaluationsWeekKey.Academic(weekNumber),
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

	return continuousItem + weeklyItems
}
