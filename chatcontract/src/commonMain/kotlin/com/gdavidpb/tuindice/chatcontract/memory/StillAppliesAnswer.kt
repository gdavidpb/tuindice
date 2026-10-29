package com.gdavidpb.tuindice.chatcontract.memory

import kotlinx.serialization.Serializable

/** Whether a remembered item still applies, as the user answers it. */
@Serializable
public data class StillAppliesAnswer(public val applies: Boolean)
