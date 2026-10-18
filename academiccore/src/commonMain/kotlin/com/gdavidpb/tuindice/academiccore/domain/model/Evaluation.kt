package com.gdavidpb.tuindice.academiccore.domain.model

/**
 * An evaluation of an attempt. [revision] is the revision the server last confirmed for it, when the reader knows it: a
 * change built on that revision holds only while the evaluation is still at it. Null when the source does not carry it.
 */
data class Evaluation(
	val id: String,
	val attemptId: String,
	val subjectCode: String,
	val termId: String,
	val scheduleMode: EvaluationScheduleMode,
	val grade: Double?,
	val maxGrade: Double,
	val date: Long?,
	val type: EvaluationType,
	val state: EvaluationState,
	val revision: Long? = null
)
