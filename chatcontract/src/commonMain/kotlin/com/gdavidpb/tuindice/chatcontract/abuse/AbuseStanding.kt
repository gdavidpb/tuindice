package com.gdavidpb.tuindice.chatcontract.abuse

import kotlinx.serialization.Serializable

/**
 * The user's standing with the assistant: its [level], and the [reason], end and appeal rights when they apply.
 * A level this build does not know reads as NONE, which restricts nothing on this side: the server still refuses
 * what it restricts, so the student only loses the explanation of the restriction.
 */
@Serializable
public data class AbuseStanding(
	public val level: StandingLevel = StandingLevel.NONE,
	public val reason: AbuseReasonCode? = null,
	public val untilMillis: Long? = null,
	public val appealable: Boolean = false
)
