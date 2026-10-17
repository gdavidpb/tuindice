package com.gdavidpb.tuindice.chatcontract.turn

import kotlinx.serialization.Serializable

/** Answer of the service once it has accepted a [TurnRequest]. */
@Serializable
public data class TurnAccepted(
	public val turnId: String,
	public val conversationId: String,
	public val lastSeq: Long
)
