package com.gdavidpb.tuindice.academiccore.domain.model

/**
 * The grades of the closed terms, one per attempt, repeated subjects included: the numeric results that carry a grade
 * from 1 to [MAX_SUBJECT_GRADE]. [countsByGrade] always holds every one of those grades, zero when none. [unreported]
 * counts the attempts whose grade was never reported.
 */
data class GradeHistogram(
	val countsByGrade: Map<Int, Int>,
	val unreported: Int
)
