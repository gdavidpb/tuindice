package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.Serializable

/** One simulated grade of a projection. */
@Serializable
public data class SimulatedGrade(
	public val attemptId: String,
	public val subjectCode: String,
	public val grade: Int? = null,
	public val outcome: String? = null
)
