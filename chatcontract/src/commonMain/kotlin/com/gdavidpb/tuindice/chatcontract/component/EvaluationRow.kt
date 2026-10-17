package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.Serializable

/** One evaluation of a list. */
@Serializable
public data class EvaluationRow(
	public val evaluationId: String,
	public val subjectCode: String,
	public val title: String,
	public val dateMillis: Long? = null,
	public val scheduleMode: String,
	public val grade: Double? = null,
	public val maxGrade: Double,
	public val state: String
)
