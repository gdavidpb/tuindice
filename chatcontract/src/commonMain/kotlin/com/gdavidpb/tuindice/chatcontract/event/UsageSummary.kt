package com.gdavidpb.tuindice.chatcontract.event

import kotlinx.serialization.Serializable

/** Requests the user has left in each time window. */
@Serializable
public data class UsageSummary(
	public val remainingHour: Int,
	public val remainingDay: Int,
	public val remainingMonth: Int
)
