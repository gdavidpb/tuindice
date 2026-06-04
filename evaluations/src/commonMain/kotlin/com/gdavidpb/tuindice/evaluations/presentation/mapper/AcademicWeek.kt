package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlin.math.max
import kotlin.math.min

internal fun EvaluationTermDescriptor.academicTermStartDate() = LocalDate(
	year = periodYear,
	month = periodCode.startMonth(),
	day = 1
)

internal fun LocalDate.academicWeekStart(): LocalDate =
	plus(DatePeriod(days = -dayOfWeek.ordinal))

internal fun computeAcademicWeek(
	term: EvaluationTermDescriptor,
	currentDate: LocalDate
): Int {
	val termStart = term.academicTermStartDate().academicWeekStart()
	val rawWeek = (termStart.daysUntil(currentDate) / DAYS_PER_WEEK) + 1
	return min(MAX_ACADEMIC_WEEK, max(MIN_ACADEMIC_WEEK, rawWeek))
}

private fun AcademicTermPeriod.startMonth(): Month = when (this) {
	AcademicTermPeriod.JAN_MAR -> Month.JANUARY
	AcademicTermPeriod.APR_JUL -> Month.APRIL
	AcademicTermPeriod.JUL_AUG -> Month.JULY
	AcademicTermPeriod.SEP_DEC -> Month.SEPTEMBER
}

internal const val DAYS_PER_WEEK = 7
internal const val MIN_ACADEMIC_WEEK = 1
internal const val MAX_ACADEMIC_WEEK = 12
