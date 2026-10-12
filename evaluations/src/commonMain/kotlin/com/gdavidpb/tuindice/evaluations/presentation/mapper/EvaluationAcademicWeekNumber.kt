package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine.MAX_ACADEMIC_WEEK
import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicWeekEngine.MIN_ACADEMIC_WEEK
import com.gdavidpb.tuindice.academiccore.domain.model.Evaluation
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.utils.toEvaluationLocalDate

/**
 * The week of the term this evaluation is listed under, or null when it has none (it is continuous
 * or has no date). The number is held between [MIN_ACADEMIC_WEEK] and [MAX_ACADEMIC_WEEK]: an
 * evaluation dated before week 1 is listed under "Week 1" and one dated after week 12 under
 * "Week 12", even though the week strip has no such day in those weeks. That is the behavior the
 * tab has always had, and it keeps an evaluation from disappearing because of its date. Without a
 * term it is week 1.
 */
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
