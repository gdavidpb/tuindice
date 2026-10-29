package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.Serializable

/** Evaluation load of one week; heavy marks a week with a high load. */
@Serializable
public data class WeekLoad(
	public val week: Int,
	public val count: Int,
	public val points: Double,
	public val heavy: Boolean
)
