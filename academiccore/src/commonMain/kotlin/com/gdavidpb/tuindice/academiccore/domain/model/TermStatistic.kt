package com.gdavidpb.tuindice.academiccore.domain.model

/**
 * One closed term. [index] is the period average the term shows, or null when it has none: the projection answers 0.0
 * when there is no grade to average. A term with a null index counts neither as the best nor as the worst term.
 * [cumulativeIndex] is the cumulative average at the close of the term, or null while nothing has been graded yet.
 * [approvedCredits] and [approvedSubjects] count the approved attempts of the term, each subject once. [isRegular] is
 * false for the July - August term, which the pace leaves out.
 */
data class TermStatistic(
	val termKey: String,
	val periodCode: AcademicTermPeriod,
	val periodYear: Int,
	val termOrder: Int,
	val kind: TermKind,
	val index: Double?,
	val cumulativeIndex: Double?,
	val approvedCredits: Int,
	val approvedSubjects: Int,
	val isRegular: Boolean
)
