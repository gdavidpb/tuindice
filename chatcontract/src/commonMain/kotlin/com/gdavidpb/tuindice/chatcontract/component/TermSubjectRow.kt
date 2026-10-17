package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.Serializable

/** One subject attempt in a term report. */
@Serializable
public data class TermSubjectRow(
	public val attemptId: String,
	public val subjectCode: String,
	public val subjectName: String,
	public val credits: Int,
	public val grade: Int? = null,
	public val outcome: String? = null,
	public val badge: String? = null
)
