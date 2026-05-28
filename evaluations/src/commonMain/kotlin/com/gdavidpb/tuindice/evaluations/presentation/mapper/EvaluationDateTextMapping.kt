package com.gdavidpb.tuindice.evaluations.presentation.mapper

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.utils.extension.capitalize
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateGroup
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.evaluation_date_next_week
import tuindice.evaluations.generated.resources.evaluation_date_past_week
import tuindice.evaluations.generated.resources.evaluation_date_this_week
import tuindice.evaluations.generated.resources.evaluation_date_today
import tuindice.evaluations.generated.resources.evaluation_date_tomorrow
import tuindice.evaluations.generated.resources.evaluation_date_weeks_ahead
import tuindice.evaluations.generated.resources.evaluation_date_yesterday
import tuindice.evaluations.generated.resources.label_evaluation_no_date

data class EvaluationDateTextMapping(
	val noDateLabel: String,
	val todayLabel: String,
	val tomorrowLabel: String,
	val yesterdayLabel: String,
	val pastWeekPattern: String,
	val thisWeekPattern: String,
	val nextWeekPattern: String,
	val weeksAheadPattern: String
)

fun EvaluationDateGroup.getLabel(mapping: EvaluationDateTextMapping): String {
	return when (this) {
		EvaluationDateGroup.Continuous -> mapping.noDateLabel
		EvaluationDateGroup.Today -> mapping.todayLabel
		EvaluationDateGroup.Tomorrow -> mapping.tomorrowLabel
		EvaluationDateGroup.Yesterday -> mapping.yesterdayLabel
		is EvaluationDateGroup.PastThisWeek ->
			mapping.pastWeekPattern.formatTextArg(
				date.formatAs(DateTextStyle.WEEKDAY_PAST_DAY_MONTH)
			)

		is EvaluationDateGroup.ThisWeek ->
			mapping.thisWeekPattern.formatTextArg(
				date.formatAs(DateTextStyle.WEEKDAY_DAY_MONTH)
			)

		is EvaluationDateGroup.NextWeek ->
			mapping.nextWeekPattern.formatTextArg(
				date.formatAs(DateTextStyle.WEEKDAY_DAY_MONTH)
			)

		is EvaluationDateGroup.WeeksAhead ->
			mapping.weeksAheadPattern.formatLongArg(weeks)

		is EvaluationDateGroup.ExactDate ->
			date.formatAs(DateTextStyle.WEEKDAY_NUMERIC_DATE).capitalize()
	}
}

@Composable
fun rememberEvaluationDateTextMapping() = EvaluationDateTextMapping(
	noDateLabel = stringResource(Res.string.label_evaluation_no_date),
	todayLabel = stringResource(Res.string.evaluation_date_today),
	tomorrowLabel = stringResource(Res.string.evaluation_date_tomorrow),
	yesterdayLabel = stringResource(Res.string.evaluation_date_yesterday),
	pastWeekPattern = stringResource(Res.string.evaluation_date_past_week),
	thisWeekPattern = stringResource(Res.string.evaluation_date_this_week),
	nextWeekPattern = stringResource(Res.string.evaluation_date_next_week),
	weeksAheadPattern = stringResource(Res.string.evaluation_date_weeks_ahead)
)

suspend fun getEvaluationDateTextMapping() = EvaluationDateTextMapping(
	noDateLabel = getString(Res.string.label_evaluation_no_date),
	todayLabel = getString(Res.string.evaluation_date_today),
	tomorrowLabel = getString(Res.string.evaluation_date_tomorrow),
	yesterdayLabel = getString(Res.string.evaluation_date_yesterday),
	pastWeekPattern = getString(Res.string.evaluation_date_past_week),
	thisWeekPattern = getString(Res.string.evaluation_date_this_week),
	nextWeekPattern = getString(Res.string.evaluation_date_next_week),
	weeksAheadPattern = getString(Res.string.evaluation_date_weeks_ahead)
)

private fun String.formatTextArg(arg: String) = replace("%1\$s", arg)

private fun String.formatLongArg(arg: Long) = replace("%1\$d", arg.toString())

private fun LocalDate.formatAs(style: DateTextStyle): String {
	return atStartOfDayIn(TimeZone.currentSystemDefault())
		.toEpochMilliseconds()
		.formatDate(style)!!
}
