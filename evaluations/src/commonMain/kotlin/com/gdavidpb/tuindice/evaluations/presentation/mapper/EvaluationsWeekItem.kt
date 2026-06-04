package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.presentation.mapper.localizedShortWeekdayNames
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.extension.currentEvaluationLocalDate
import com.gdavidpb.tuindice.evaluations.presentation.extension.toEvaluationLocalDate
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationWeekDayItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlin.math.max
import kotlin.math.min

fun buildEvaluationsWeekItem(
	currentTerm: EvaluationTermDescriptor?,
	evaluations: List<Evaluation>,
	weekLabelPattern: String,
	currentDate: LocalDate = currentEvaluationLocalDate()
): EvaluationsWeekItem {
	val defaultWeekNumber = defaultEvaluationsWeekNumber(
		currentTerm = currentTerm,
		currentDate = currentDate
	)

	return buildEvaluationsWeekItems(
		currentTerm = currentTerm,
		evaluations = evaluations,
		weekLabelPattern = weekLabelPattern,
		currentDate = currentDate
	).first { item ->
		item.weekNumber == defaultWeekNumber
	}
}

fun buildEvaluationsWeekItems(
	currentTerm: EvaluationTermDescriptor?,
	evaluations: List<Evaluation>,
	weekLabelPattern: String,
	currentDate: LocalDate = currentEvaluationLocalDate()
): List<EvaluationsWeekItem> {
	val termStart = currentTerm?.startDate() ?: currentDate
	val evaluationDates = evaluations.mapNotNull { evaluation ->
		evaluation.date?.toEvaluationLocalDate()
	}.toSet()
	val weekdayNames = localizedShortWeekdayNames()

	return (MIN_ACADEMIC_WEEK..MAX_ACADEMIC_WEEK).map { weekNumber ->
		val weekStart = termStart
			.plus(DatePeriod(days = (weekNumber - 1) * DAYS_PER_WEEK))
			.weekStart()

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

fun defaultEvaluationsWeekNumber(
	currentTerm: EvaluationTermDescriptor?,
	currentDate: LocalDate = currentEvaluationLocalDate()
): Int {
	return currentTerm?.let { term ->
		computeAcademicWeek(
			term = term,
			currentDate = currentDate
		)
	} ?: MIN_ACADEMIC_WEEK
}

fun computeAcademicWeek(
	term: EvaluationTermDescriptor,
	currentDate: LocalDate
): Int {
	val termStart = term.startDate().weekStart()
	val rawWeek = (termStart.daysUntil(currentDate) / DAYS_PER_WEEK) + 1
	return min(MAX_ACADEMIC_WEEK, max(MIN_ACADEMIC_WEEK, rawWeek))
}

fun Evaluation.academicWeekNumber(currentTerm: EvaluationTermDescriptor?): Int? {
	val evaluationDate = date?.toEvaluationLocalDate() ?: return null
	return currentTerm?.let { term ->
		computeAcademicWeek(
			term = term,
			currentDate = evaluationDate
		)
	} ?: MIN_ACADEMIC_WEEK
}

private fun LocalDate.weekStart(): LocalDate =
	plus(DatePeriod(days = -dayOfWeek.ordinal))

private fun EvaluationTermDescriptor.startDate() = LocalDate(
	year = periodYear,
	month = periodCode.startMonth(),
	day = 1
)

private fun AcademicTermPeriod.startMonth(): Month = when (this) {
	AcademicTermPeriod.JAN_MAR -> Month.JANUARY
	AcademicTermPeriod.APR_JUL -> Month.APRIL
	AcademicTermPeriod.JUL_AUG -> Month.JULY
	AcademicTermPeriod.SEP_DEC -> Month.SEPTEMBER
}

private const val DAYS_PER_WEEK = 7
private const val MIN_ACADEMIC_WEEK = 1
private const val MAX_ACADEMIC_WEEK = 12
