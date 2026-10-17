package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.Serializable

/** Points of one subject: earned, evaluated so far, and total. */
@Serializable
public data class SubjectPointsRow(
	public val subjectCode: String,
	public val subjectName: String,
	public val earned: Double,
	public val evaluated: Double,
	public val total: Double
)
