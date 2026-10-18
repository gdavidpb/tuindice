package com.gdavidpb.tuindice.chatcontract.event

import com.gdavidpb.tuindice.chatcontract.intent.IntentStatus
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The intent [intentId] ended with [outcome]. [status] is the final status the server keeps for it, when the
 * outcome alone does not name it: a proposal another one replaced ends superseded, and its outcome reads as
 * cancelled.
 */
@Serializable
@SerialName("intent_resolved")
public data class IntentResolved(
	override val seq: Long,
	override val turnId: String,
	public val intentId: String,
	public val outcome: OutcomeKind,
	public val status: IntentStatus? = null
) : AssistantEvent
