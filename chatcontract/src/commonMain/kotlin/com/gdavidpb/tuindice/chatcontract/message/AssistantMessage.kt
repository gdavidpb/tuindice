package com.gdavidpb.tuindice.chatcontract.message

import kotlinx.serialization.Serializable

/** Message the assistant wrote in a turn, made of [parts] in display order. */
@Serializable
public data class AssistantMessage(
	public val id: String,
	public val parts: List<MessagePart>,
	public val createdAtMillis: Long
)
