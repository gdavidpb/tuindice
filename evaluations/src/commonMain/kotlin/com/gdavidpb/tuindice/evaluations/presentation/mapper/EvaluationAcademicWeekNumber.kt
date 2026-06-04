package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.extension.toEvaluationLocalDate

fun Evaluation.academicWeekNumber(currentTerm: EvaluationTermDescriptor?): Int? {
	val evaluationDate = date?.toEvaluationLocalDate() ?: return null
	return currentTerm?.let { term ->
		computeAcademicWeek(
			term = term,
			currentDate = evaluationDate
		)
	} ?: MIN_ACADEMIC_WEEK
}
