package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.model.UiText

fun Evaluation.formatAsDayOfWeekAndDate(noDateLabel: UiText): UiText {
	if (scheduleMode == EvaluationScheduleMode.CONTINUOUS) return noDateLabel

	return date.formatAsDayOfWeekAndDate(noDateLabel = noDateLabel)
}

// "Jueves — 15/01/26"
private fun Long?.formatAsDayOfWeekAndDate(noDateLabel: UiText): UiText {
	if (this == null) return noDateLabel

	return UiText.Capitalized(formatDate(DateTextStyle.WEEKDAY_NUMERIC_DATE))
}

// "Jue — 15/01/26"
fun Long.formatAsShortDayOfWeekAndDate(): UiText {
	return UiText.Capitalized(formatDate(DateTextStyle.SHORT_WEEKDAY_NUMERIC_DATE))
}
