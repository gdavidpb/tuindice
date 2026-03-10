package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.daysToNow
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.mapper.weeksToNow
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.base.utils.extension.capitalize
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateGroup
import com.gdavidpb.tuindice.evaluations.presentation.extension.toEvaluationLocalDate

fun Evaluation.toEvaluationDateGroup(): EvaluationDateGroup {
	if (scheduleMode == EvaluationScheduleMode.CONTINUOUS) return EvaluationDateGroup.Continuous

	return date.toEvaluationDateGroup()
}

fun Long?.toEvaluationDateGroup(): EvaluationDateGroup {
	val evaluationDate = this?.toEvaluationLocalDate() ?: return EvaluationDateGroup.Continuous
	val daysDistance = daysToNow()
	val weeksDistance = weeksToNow()

	return when {
		daysDistance == 0 -> EvaluationDateGroup.Today
		daysDistance == 1 -> EvaluationDateGroup.Tomorrow
		daysDistance == -1 -> EvaluationDateGroup.Yesterday
		weeksDistance == 0L -> {
			if (this < currentTimeMillis())
				EvaluationDateGroup.PastThisWeek(evaluationDate)
			else
				EvaluationDateGroup.ThisWeek(evaluationDate)
		}

		weeksDistance == 1L -> EvaluationDateGroup.NextWeek(evaluationDate)
		weeksDistance in 2..12 -> EvaluationDateGroup.WeeksAhead(weeksDistance)
		else -> EvaluationDateGroup.ExactDate(evaluationDate)
	}
}

fun Evaluation.formatAsDayOfWeekAndDate(noDateLabel: String): String {
	if (scheduleMode == EvaluationScheduleMode.CONTINUOUS) return noDateLabel

	return date.formatAsDayOfWeekAndDate(noDateLabel = noDateLabel)
}

private fun Long?.formatAsDayOfWeekAndDate(noDateLabel: String): String {
	if (this == null) return noDateLabel

	return formatDate(DateTextStyle.WEEKDAY_NUMERIC_DATE)?.capitalize()!!
}

fun Long.formatAsShortDayOfWeekAndDate(): String {
	return formatDate(DateTextStyle.SHORT_WEEKDAY_NUMERIC_DATE)?.capitalize()!!
}
