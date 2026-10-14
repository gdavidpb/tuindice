package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod

/**
 * "Este trimestre": the term in progress as its evaluations tell it, for the day it is read on.
 * [evaluationTypes] holds the weighted performance of each type the term has. [evaluatedFraction] is how much of the
 * points of its subjects the evaluations already cover, null while that is not known. [overdueWithoutGrade] counts the
 * evaluations that went by without a grade.
 *
 * [hasSyncedEvaluations] says whether the evaluations were downloaded. Until they were, nothing is known about them:
 * the lists are empty, the count is zero and the fraction is null, and the screen invites to open them in Evaluations.
 */
data class SummaryCurrentTermStatistics(
	val periodCode: AcademicTermPeriod,
	val periodYear: Int,
	val evaluationTypes: List<SummaryEvaluationTypeStatistic>,
	val evaluatedFraction: Double?,
	val overdueWithoutGrade: Int,
	val hasSyncedEvaluations: Boolean
)
