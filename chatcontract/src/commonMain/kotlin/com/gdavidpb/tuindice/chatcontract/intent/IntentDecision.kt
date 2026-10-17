package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.Serializable

/** The user's decision on an intent: accepted or not, optionally with [editedArgs]. */
@Serializable
public data class IntentDecision(
	public val intentId: String,
	public val accepted: Boolean,
	public val editedArgs: IntentArgs? = null
)
