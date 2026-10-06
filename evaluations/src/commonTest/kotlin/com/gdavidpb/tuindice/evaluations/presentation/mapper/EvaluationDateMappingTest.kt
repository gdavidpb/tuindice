package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.model.UiText
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
