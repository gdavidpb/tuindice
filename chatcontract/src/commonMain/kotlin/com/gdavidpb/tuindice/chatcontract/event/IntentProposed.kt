package com.gdavidpb.tuindice.chatcontract.event

import com.gdavidpb.tuindice.chatcontract.intent.Intent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The assistant proposes [intent] in the message [messageId]; the user confirms or cancels it. */
@Serializable
@SerialName("intent_proposed")
public data class IntentProposed(
	override val seq: Long,
	override val turnId: String,
	public val messageId: String,
	public val intent: Intent
) : AssistantEvent
