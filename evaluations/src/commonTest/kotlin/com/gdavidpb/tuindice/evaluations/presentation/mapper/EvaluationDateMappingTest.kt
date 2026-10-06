package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.mapper.toNameText
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateGroup
import com.gdavidpb.tuindice.evaluations.presentation.utils.currentEvaluationLocalDate
import com.gdavidpb.tuindice.evaluations.presentation.utils.toEvaluationEpochMillis
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.plus
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
import kotlin.test.Test
import kotlin.test.assertEquals

class EvaluationDateMappingTest {
	@Test
	fun toEvaluationDateGroup_whenEvaluationIsContinuous_ignoresDateAndReturnsContinuous() {
		val evaluation = DEFAULT_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.CONTINUOUS,
			date = daysFromToday(2)
		)

		assertEquals(EvaluationDateGroup.Continuous, evaluation.toEvaluationDateGroup())
	}

	@Test
	fun toEvaluationDateGroup_whenDateIsNull_returnsContinuous() {
		assertEquals(EvaluationDateGroup.Continuous, (null as Long?).toEvaluationDateGroup())
	}

	@Test
	fun toEvaluationDateGroup_whenDateIsAroundToday_returnsRelativeDayGroups() {
		assertEquals(EvaluationDateGroup.Today, daysFromToday(0).toEvaluationDateGroup())
		assertEquals(EvaluationDateGroup.Tomorrow, daysFromToday(1).toEvaluationDateGroup())
		assertEquals(EvaluationDateGroup.Yesterday, daysFromToday(-1).toEvaluationDateGroup())
	}

	@Test
	fun toEvaluationDateGroup_whenDateIsWithinAWeek_returnsThisWeekGroups() {
		assertEquals(
			EvaluationDateGroup.ThisWeek(localDateFromToday(2)),
			daysFromToday(2).toEvaluationDateGroup()
		)
		assertEquals(
			EvaluationDateGroup.PastThisWeek(localDateFromToday(-2)),
			daysFromToday(-2).toEvaluationDateGroup()
		)
	}

	@Test
	fun toEvaluationDateGroup_whenDateIsBetweenSevenAndThirteenDaysAhead_returnsNextWeek() {
		assertEquals(
			EvaluationDateGroup.NextWeek(localDateFromToday(7)),
			daysFromToday(7).toEvaluationDateGroup()
		)
		assertEquals(
			EvaluationDateGroup.NextWeek(localDateFromToday(13)),
			daysFromToday(13).toEvaluationDateGroup()
		)
	}

	@Test
	fun toEvaluationDateGroup_whenDateIsWeeksAhead_returnsWeeksAheadUpToTwelve() {
		assertEquals(
			EvaluationDateGroup.WeeksAhead(weeks = 2L),
			daysFromToday(14).toEvaluationDateGroup()
		)
		assertEquals(
			EvaluationDateGroup.WeeksAhead(weeks = 12L),
			daysFromToday(12 * 7).toEvaluationDateGroup()
		)
	}

	@Test
	fun toEvaluationDateGroup_whenDateIsBeyondTrackedWeeks_returnsExactDate() {
		assertEquals(
			EvaluationDateGroup.ExactDate(localDateFromToday(13 * 7)),
			daysFromToday(13 * 7).toEvaluationDateGroup()
		)
	}

	@Test
	fun toEvaluationDateGroup_whenDateIsAWeekOrMoreInThePast_returnsExactDate() {
		assertEquals(
			EvaluationDateGroup.ExactDate(localDateFromToday(-7)),
			daysFromToday(-7).toEvaluationDateGroup()
		)
	}

	@Test
	fun getLabel_whenGroupIsARelativeDayOrContinuous_describesItsOwnLabel() {
		assertEquals(
			UiText.Resource(Res.string.label_evaluation_no_date),
			EvaluationDateGroup.Continuous.getLabel()
		)
		assertEquals(UiText.Resource(Res.string.evaluation_date_today), EvaluationDateGroup.Today.getLabel())
		assertEquals(UiText.Resource(Res.string.evaluation_date_tomorrow), EvaluationDateGroup.Tomorrow.getLabel())
		assertEquals(UiText.Resource(Res.string.evaluation_date_yesterday), EvaluationDateGroup.Yesterday.getLabel())
	}

	@Test
	fun getLabel_whenGroupIsADayOfAWeek_describesItsPatternWithTheDateInside() {
		val date = LocalDate(2026, 1, 15)
		val millis = date.toEvaluationEpochMillis()

		assertEquals(
			UiText.Resource(
				Res.string.evaluation_date_past_week,
				listOf(millis.formatDate(DateTextStyle.WEEKDAY_PAST_DAY_MONTH))
			),
			EvaluationDateGroup.PastThisWeek(date).getLabel()
		)
		assertEquals(
			UiText.Resource(
				Res.string.evaluation_date_this_week,
				listOf(millis.formatDate(DateTextStyle.WEEKDAY_DAY_MONTH))
			),
			EvaluationDateGroup.ThisWeek(date).getLabel()
		)
		assertEquals(
			UiText.Resource(
				Res.string.evaluation_date_next_week,
				listOf(millis.formatDate(DateTextStyle.WEEKDAY_DAY_MONTH))
			),
			EvaluationDateGroup.NextWeek(date).getLabel()
		)
	}

	@Test
	fun getLabel_whenGroupIsWeeksAheadOrAnExactDate_describesTheCountOrTheCapitalizedDate() {
		val date = LocalDate(2026, 1, 15)

		assertEquals(
			UiText.Resource(Res.string.evaluation_date_weeks_ahead, listOf(3L)),
			EvaluationDateGroup.WeeksAhead(weeks = 3).getLabel()
		)
		assertEquals(
			UiText.Capitalized(date.toEvaluationEpochMillis().formatDate(DateTextStyle.WEEKDAY_NUMERIC_DATE)),
			EvaluationDateGroup.ExactDate(date).getLabel()
		)
	}

	@Test
	fun formatAsDayOfWeekAndDate_whenEvaluationIsContinuousOrUndated_returnsNoDateLabel() {
		val continuousEvaluation = DEFAULT_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.CONTINUOUS,
			date = daysFromToday(2)
		)
		val undatedEvaluation = DEFAULT_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.DATED,
			date = null
		)

		assertEquals(NO_DATE_LABEL, continuousEvaluation.formatAsDayOfWeekAndDate(NO_DATE_LABEL))
		assertEquals(NO_DATE_LABEL, undatedEvaluation.formatAsDayOfWeekAndDate(NO_DATE_LABEL))
	}

	@Test
	fun formatAsDayOfWeekAndDate_whenEvaluationIsDated_describesTheCapitalizedWeekdayAndNumericDate() {
		val date = LocalDate(2026, 1, 15).toEvaluationEpochMillis()
		val evaluation = DEFAULT_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.DATED,
			date = date
		)

		assertEquals(
			UiText.Capitalized(date.formatDate(DateTextStyle.WEEKDAY_NUMERIC_DATE)),
			evaluation.formatAsDayOfWeekAndDate(NO_DATE_LABEL)
		)
	}

	@Test
	fun formatAsExactDateHeader_whenEvaluationIsContinuousOrUndated_returnsNoDateLabel() {
		val continuousEvaluation = DEFAULT_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.CONTINUOUS,
			date = daysFromToday(2)
		)
		val undatedEvaluation = DEFAULT_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.DATED,
			date = null
		)

		assertEquals(NO_DATE_LABEL, continuousEvaluation.formatAsExactDateHeader(NO_DATE_LABEL))
		assertEquals(NO_DATE_LABEL, undatedEvaluation.formatAsExactDateHeader(NO_DATE_LABEL))
	}

	@Test
	fun formatAsExactDateHeader_whenEvaluationIsDated_describesTheCapitalizedWeekdayTheDayAndTheCapitalizedMonth() {
		val evaluation = DEFAULT_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.DATED,
			date = LocalDate(2026, 2, 4).toEvaluationEpochMillis()
		)

		assertEquals(
			UiText.Resource(
				Res.string.evaluation_date_exact_header,
				listOf(
					UiText.Capitalized(DayOfWeek.WEDNESDAY.toNameText()),
					"4",
					UiText.Capitalized(Month.FEBRUARY.toNameText())
				)
			),
			evaluation.formatAsExactDateHeader(NO_DATE_LABEL)
		)
	}

	@Test
	fun formatAsShortDayOfWeekAndDate_whenDateIsKnown_describesTheCapitalizedShortWeekdayAndNumericDate() {
		val date = LocalDate(2026, 1, 15).toEvaluationEpochMillis()

		assertEquals(
			UiText.Capitalized(date.formatDate(DateTextStyle.SHORT_WEEKDAY_NUMERIC_DATE)),
			date.formatAsShortDayOfWeekAndDate()
		)
	}

	private fun localDateFromToday(days: Int): LocalDate {
		return currentEvaluationLocalDate().plus(DatePeriod(days = days))
	}

	private fun daysFromToday(days: Int): Long {
		return localDateFromToday(days).toEvaluationEpochMillis()
	}
}

private val NO_DATE_LABEL = UiText.Raw("Sin fecha")
