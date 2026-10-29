package com.gdavidpb.tuindice.chatcontract.component

import kotlinx.serialization.Serializable

/** Credits taken in one term. */
@Serializable
public data class TermCredits(
	public val termLabel: String,
	public val credits: Int
)
