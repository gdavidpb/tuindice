package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import kotlinx.datetime.LocalDate

/**
 * The week of the screen that [date] falls in, or null when it falls in none: before week 1, after
 * the last week, or while the screen does not know where week 1 starts ([weekOneStart]).
 */
fun evaluationsWeekKeyContaining(
	weekOneStart: LocalDate?,
	date: LocalDate
): EvaluationsWeekKey? {
	if (weekOneStart == null) return null

	return AcademicWeekEngine.weekOrNull(weekOneStart = weekOneStart, date = date)
		?.let { weekNumber -> EvaluationsWeekKey.Academic(weekNumber) }
}
