package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import kotlinx.datetime.LocalDate

/**
 * The week of the screen that the list files [date] under, or null while the screen does not know
 * where week 1 starts ([weekOneStart]). It is the rule of the list itself: a date before week 1 is
 * listed under week 1 and one after the last week under the last, so a jump to a Monday outside the
 * term lands where the evaluations marked on that day are listed.
 */
fun evaluationsWeekKeyContaining(
	weekOneStart: LocalDate?,
	date: LocalDate
): EvaluationsWeekKey? {
	if (weekOneStart == null) return null

	return EvaluationsWeekKey.Academic(AcademicWeekEngine.weekOf(weekOneStart = weekOneStart, date = date))
}
