package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.Serializable

/**
 * One evaluation to add. [evaluationType] is the name of academiccore's EvaluationType, such as "QUIZ".
 * [scheduleMode] is "dated" or "continuous".
 */
@Serializable
public data class EvaluationDraft(
	public val mutationId: String,
	public val reference: String,
	public val attemptId: String,
	public val subjectCode: String,
	public val termId: String,
	public val evaluationType: String,
	public val scheduleMode: String,
	public val dateMillis: Long? = null,
	public val grade: Double? = null,
	public val maxGrade: Double,
	public val selected: Boolean = true
)
