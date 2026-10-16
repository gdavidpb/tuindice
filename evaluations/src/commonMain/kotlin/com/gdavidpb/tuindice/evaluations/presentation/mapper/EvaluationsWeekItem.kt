package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine.DAYS_PER_WEEK
import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine.MAX_ACADEMIC_WEEK
import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine.MIN_ACADEMIC_WEEK
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.presentation.mapper.toShortNameText
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.utils.toLocalDate
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationWeekDayItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

fun buildEvaluationsWeekItems(
	currentTerm: EvaluationTermDescriptor?,
	evaluations: List<Evaluation>,
	weekLabelPattern: String,
	continuousLabel: String,
	currentDate: LocalDate
): List<EvaluationsWeekItem> {
	val termStart = evaluationsWeekOneStart(currentTerm = currentTerm, currentDate = currentDate)
	val evaluationDates = evaluations.mapNotNull { evaluation ->
		if (evaluation.scheduleMode == EvaluationScheduleMode.CONTINUOUS) {
			null
		} else {
			evaluation.date?.toLocalDate()
		}
	}.toSet()
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
		val weekStart = termStart.plus(DatePeriod(days = (weekNumber - 1) * DAYS_PER_WEEK))

		EvaluationsWeekItem(
			key = EvaluationsWeekKey.Academic(weekNumber),
			labelText = weekLabelPattern.replace("%1${'$'}d", weekNumber.toString()),
			days = (0..6).map { offset ->
				val date = weekStart.plus(DatePeriod(days = offset))
				EvaluationWeekDayItem(
					weekdayText = UiText.Uppercase(date.dayOfWeek.toShortNameText()),
					dayText = date.day.toString(),
					isSelected = date == currentDate,
					hasEvaluations = date in evaluationDates
				)
			}
		)
	}

	return continuousItem + weeklyItems
}
