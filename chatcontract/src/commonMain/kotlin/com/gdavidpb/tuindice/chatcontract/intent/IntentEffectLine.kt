package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.Serializable

/** One line of what an intent will do, shown to the user before they confirm it. */
@Serializable
public data class IntentEffectLine(public val label: String, public val detail: String)
