package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod

/**
 * The current term: its period, the academic week it is (null outside weeks 1 to 12) and the points
 * of each of its subjects, which are empty while the evaluations are not downloaded, because
 * saying a subject has no grades would not be known.
 */
data class SummaryTerm(
	val periodCode: AcademicTermPeriod,
	val periodYear: Int,
	val academicWeek: Int?,
	val subjects: List<SummarySubjectPoints>
)
