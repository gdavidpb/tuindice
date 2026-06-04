package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.extension.toEvaluationLocalDate

fun Evaluation.academicWeekNumber(currentTerm: EvaluationTermDescriptor?): Int? {
	if (scheduleMode == EvaluationScheduleMode.CONTINUOUS) return null

	val evaluationDate = date?.toEvaluationLocalDate() ?: return null
	return currentTerm?.let { term ->
		computeAcademicWeek(
			term = term,
			currentDate = evaluationDate
		)
	} ?: MIN_ACADEMIC_WEEK
}
