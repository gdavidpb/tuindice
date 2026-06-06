package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.extension.currentEvaluationLocalDate
import kotlinx.datetime.LocalDate

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
