package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

internal fun EvaluationTermDescriptor.academicTermStartDate() = LocalDate(
	year = periodYear,
	month = periodCode.startMonth,
	day = 1
)

// The Monday that opens week 1: it follows the real first day of classes when the backend served a
// plausible one, and the calendar month of the period otherwise. Every week number and the strip of
// weeks must come from this one anchor.
internal fun EvaluationTermDescriptor.academicWeekOneStart(): LocalDate = AcademicWeekEngine.weekOneStart(
	periodYear = periodYear,
	periodCode = periodCode,
	classesStartDate = classesStartDate
)

internal fun LocalDate.academicWeekStart(): LocalDate =
	plus(DatePeriod(days = -dayOfWeek.ordinal))

internal fun computeAcademicWeek(
	term: EvaluationTermDescriptor,
	currentDate: LocalDate
): Int {
	return AcademicWeekEngine.weekOf(weekOneStart = term.academicWeekOneStart(), date = currentDate)
}

internal const val DAYS_PER_WEEK = AcademicWeekEngine.DAYS_PER_WEEK
internal const val MIN_ACADEMIC_WEEK = AcademicWeekEngine.MIN_ACADEMIC_WEEK
internal const val MAX_ACADEMIC_WEEK = AcademicWeekEngine.MAX_ACADEMIC_WEEK
