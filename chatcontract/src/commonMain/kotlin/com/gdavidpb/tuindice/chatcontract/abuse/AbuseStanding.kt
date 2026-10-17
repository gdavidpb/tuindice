package com.gdavidpb.tuindice.chatcontract.abuse

import kotlinx.serialization.Serializable

/** The user's standing with the assistant: its [level], and the [reason], end and appeal rights when they apply. */
@Serializable
public data class AbuseStanding(
	public val level: StandingLevel,
	public val reason: AbuseReasonCode? = null,
	public val untilMillis: Long? = null,
	public val appealable: Boolean = false
)
