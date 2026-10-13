package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationPoints

/** The points of a subject of the term; null [points] when none of its evaluations has a grade. */
data class SummarySubjectPoints(
	val subjectCode: String,
	val points: EvaluationPoints?
)
