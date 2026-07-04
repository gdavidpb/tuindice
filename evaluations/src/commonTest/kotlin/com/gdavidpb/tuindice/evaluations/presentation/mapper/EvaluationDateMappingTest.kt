package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationDateGroup
import com.gdavidpb.tuindice.evaluations.presentation.utils.currentEvaluationLocalDate
import com.gdavidpb.tuindice.evaluations.presentation.utils.toEvaluationEpochMillis
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
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
	fun formatAsDayOfWeekAndDate_whenEvaluationIsDated_formatsCapitalizedWeekdayAndNumericDate() {
		val evaluation = DEFAULT_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.DATED,
			date = LocalDate(2026, 1, 15).toEvaluationEpochMillis()
		)

		assertEquals("Jueves — 15/01/26", evaluation.formatAsDayOfWeekAndDate(NO_DATE_LABEL))
	}

	@Test
	fun formatAsExactDateHeader_whenEvaluationIsContinuous_returnsNoDateLabel() {
		val evaluation = DEFAULT_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.CONTINUOUS,
			date = null
		)

		assertEquals(NO_DATE_LABEL, evaluation.formatAsExactDateHeader(NO_DATE_LABEL))
	}

	@Test
	fun formatAsExactDateHeader_whenEvaluationIsDated_formatsWeekdayDayAndMonth() {
		val evaluation = DEFAULT_PENDING_EVALUATION.copy(
			scheduleMode = EvaluationScheduleMode.DATED,
			date = LocalDate(2026, 1, 15).toEvaluationEpochMillis()
		)

		assertEquals("Jueves 15 de Enero", evaluation.formatAsExactDateHeader(NO_DATE_LABEL))
	}

	@Test
	fun formatAsShortDayOfWeekAndDate_whenDateIsKnown_formatsCapitalizedShortWeekday() {
		val date = LocalDate(2026, 1, 15).toEvaluationEpochMillis()

		assertEquals("Jue — 15/01/26", date.formatAsShortDayOfWeekAndDate())
	}

	private fun localDateFromToday(days: Int): LocalDate {
		return currentEvaluationLocalDate().plus(DatePeriod(days = days))
	}

	private fun daysFromToday(days: Int): Long {
		return localDateFromToday(days).toEvaluationEpochMillis()
	}
}

private const val NO_DATE_LABEL = "Sin fecha"
