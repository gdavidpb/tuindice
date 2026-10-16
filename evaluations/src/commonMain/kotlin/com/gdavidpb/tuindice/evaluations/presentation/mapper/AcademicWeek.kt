package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import kotlinx.datetime.LocalDate

// The Monday that opens week 1: it follows the real first day of classes when the backend served a
// plausible one, and the calendar month of the period otherwise. Every week number and the strip of
// weeks must come from this one anchor.
internal fun EvaluationTermDescriptor.academicWeekOneStart(): LocalDate = AcademicWeekEngine.weekOneStart(
	periodYear = periodYear,
	periodCode = periodCode,
	classesStartDate = classesStartDate
)

// The anchor the strip of weeks hangs from. Without a term there is no week 1: the strip is laid out
// from the Monday of the week of [currentDate], and that anchor moves with the week.
internal fun evaluationsWeekOneStart(
	currentTerm: EvaluationTermDescriptor?,
	currentDate: LocalDate
): LocalDate {
	return currentTerm?.academicWeekOneStart()
		?: AcademicWeekEngine.mondayOf(currentDate)
}

internal fun computeAcademicWeek(
	term: EvaluationTermDescriptor,
	currentDate: LocalDate
): Int {
	return AcademicWeekEngine.weekOf(weekOneStart = term.academicWeekOneStart(), date = currentDate)
}
