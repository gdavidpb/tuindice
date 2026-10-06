package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.daysToNow
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.mapper.toNameText
import com.gdavidpb.tuindice.base.presentation.mapper.weeksToNow
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateGroup
import com.gdavidpb.tuindice.evaluations.presentation.utils.toEvaluationEpochMillis
import com.gdavidpb.tuindice.evaluations.presentation.utils.toEvaluationLocalDate
import kotlinx.datetime.LocalDate
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.evaluation_date_exact_header
import tuindice.evaluations.generated.resources.evaluation_date_next_week
import tuindice.evaluations.generated.resources.evaluation_date_past_week
import tuindice.evaluations.generated.resources.evaluation_date_this_week
import tuindice.evaluations.generated.resources.evaluation_date_today
import tuindice.evaluations.generated.resources.evaluation_date_tomorrow
import tuindice.evaluations.generated.resources.evaluation_date_weeks_ahead
import tuindice.evaluations.generated.resources.evaluation_date_yesterday
import tuindice.evaluations.generated.resources.label_evaluation_no_date

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

// The title of a date group: a relative day, a day of this week or the next one with its date,
// a count of weeks, or the date itself.
fun EvaluationDateGroup.getLabel(): UiText {
	return when (this) {
		EvaluationDateGroup.Continuous -> UiText.Resource(Res.string.label_evaluation_no_date)
		EvaluationDateGroup.Today -> UiText.Resource(Res.string.evaluation_date_today)
		EvaluationDateGroup.Tomorrow -> UiText.Resource(Res.string.evaluation_date_tomorrow)
		EvaluationDateGroup.Yesterday -> UiText.Resource(Res.string.evaluation_date_yesterday)
		is EvaluationDateGroup.PastThisWeek ->
			UiText.Resource(
				Res.string.evaluation_date_past_week,
				listOf(date.formatAs(DateTextStyle.WEEKDAY_PAST_DAY_MONTH))
			)

		is EvaluationDateGroup.ThisWeek ->
			UiText.Resource(
				Res.string.evaluation_date_this_week,
				listOf(date.formatAs(DateTextStyle.WEEKDAY_DAY_MONTH))
			)

		is EvaluationDateGroup.NextWeek ->
			UiText.Resource(
				Res.string.evaluation_date_next_week,
				listOf(date.formatAs(DateTextStyle.WEEKDAY_DAY_MONTH))
			)

		is EvaluationDateGroup.WeeksAhead ->
			UiText.Resource(Res.string.evaluation_date_weeks_ahead, listOf(weeks))

		is EvaluationDateGroup.ExactDate ->
			UiText.Capitalized(date.formatAs(DateTextStyle.WEEKDAY_NUMERIC_DATE))
	}
}

fun Evaluation.formatAsDayOfWeekAndDate(noDateLabel: UiText): UiText {
	if (scheduleMode == EvaluationScheduleMode.CONTINUOUS) return noDateLabel

	return date.formatAsDayOfWeekAndDate(noDateLabel = noDateLabel)
}

fun Evaluation.formatAsExactDateHeader(noDateLabel: UiText): UiText {
	if (scheduleMode == EvaluationScheduleMode.CONTINUOUS) return noDateLabel

	return date.formatAsExactDateHeader(noDateLabel = noDateLabel)
}

// "Jueves — 15/01/26"
private fun Long?.formatAsDayOfWeekAndDate(noDateLabel: UiText): UiText {
	if (this == null) return noDateLabel

	return UiText.Capitalized(formatDate(DateTextStyle.WEEKDAY_NUMERIC_DATE))
}

// "Jueves 15 de Enero": the weekday and the month each start in upper case.
private fun Long?.formatAsExactDateHeader(noDateLabel: UiText): UiText {
	val localDate = this?.toEvaluationLocalDate() ?: return noDateLabel

	return UiText.Resource(
		Res.string.evaluation_date_exact_header,
		listOf(
			UiText.Capitalized(localDate.dayOfWeek.toNameText()),
			localDate.day.toString(),
			UiText.Capitalized(localDate.month.toNameText())
		)
	)
}

// "Jue — 15/01/26"
fun Long.formatAsShortDayOfWeekAndDate(): UiText {
	return UiText.Capitalized(formatDate(DateTextStyle.SHORT_WEEKDAY_NUMERIC_DATE))
}

private fun LocalDate.formatAs(style: DateTextStyle): UiText {
	return toEvaluationEpochMillis().formatDate(style)
}
