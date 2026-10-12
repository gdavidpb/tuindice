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

internal fun LocalDate.academicWeekStart(): LocalDate =
	plus(DatePeriod(days = -dayOfWeek.ordinal))

internal fun computeAcademicWeek(
	term: EvaluationTermDescriptor,
	currentDate: LocalDate
): Int {
	val weekOneStart = AcademicWeekEngine.weekOneStart(
		periodYear = term.periodYear,
		periodCode = term.periodCode
	)

	return AcademicWeekEngine.weekOf(weekOneStart = weekOneStart, date = currentDate)
}

internal const val DAYS_PER_WEEK = AcademicWeekEngine.DAYS_PER_WEEK
internal const val MIN_ACADEMIC_WEEK = AcademicWeekEngine.MIN_ACADEMIC_WEEK
internal const val MAX_ACADEMIC_WEEK = AcademicWeekEngine.MAX_ACADEMIC_WEEK
