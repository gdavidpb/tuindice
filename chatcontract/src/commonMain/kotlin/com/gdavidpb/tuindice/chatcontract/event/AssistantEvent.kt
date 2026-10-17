package com.gdavidpb.tuindice.chatcontract.event

import kotlinx.serialization.Serializable

/** Event streamed for a turn; [seq] is its position in the stream. */
@Serializable
public sealed interface AssistantEvent {
	public val seq: Long
	public val turnId: String
}
