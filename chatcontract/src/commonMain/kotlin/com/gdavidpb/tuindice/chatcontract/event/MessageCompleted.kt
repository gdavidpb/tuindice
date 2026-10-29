package com.gdavidpb.tuindice.chatcontract.event

import com.gdavidpb.tuindice.chatcontract.message.AssistantMessage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The assistant message of the turn is complete. */
@Serializable
@SerialName("message_completed")
public data class MessageCompleted(
	override val seq: Long,
	override val turnId: String,
	public val message: AssistantMessage
) : AssistantEvent
