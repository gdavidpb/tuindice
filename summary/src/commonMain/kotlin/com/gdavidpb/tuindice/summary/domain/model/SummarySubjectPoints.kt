package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationPoints
import kotlin.math.min

/** The points a subject is out of: the convention of the USB, the same 100 for every subject. */
const val SUBJECT_POINTS_TOTAL = 100.0

/**
 * A subject of the term, on its attempt [attemptId], as the hub says it: its [credits], its [points] and the
 * [nextEvaluation] it has without a grade, if any. [points] is null while none of its evaluations is registered;
 * with evaluations but none graded, its [EvaluationPoints.gradedCount] is zero.
 */
data class SummarySubjectPoints(
	val subjectCode: String,
	val points: EvaluationPoints?,
	val attemptId: String,
	val credits: Int,
	val nextEvaluation: SummaryNextEvaluation? = null
) {
	/**
	 * What the subject has evaluated of its [SUBJECT_POINTS_TOTAL]: the maximum grades of its graded evaluations,
	 * held at the total, since a subject cannot be evaluated past it.
	 */
	val evaluatedOfTotal: Double
		get() = min(points?.evaluated ?: 0.0, SUBJECT_POINTS_TOTAL)

	/**
	 * What the subject has earned of its [SUBJECT_POINTS_TOTAL]: the points its graded evaluations add up to, held at
	 * what it has evaluated, since a subject cannot have earned more than it is evaluated of.
	 */
	val earnedOfTotal: Double
		get() = min(points?.earned ?: 0.0, evaluatedOfTotal)
}
