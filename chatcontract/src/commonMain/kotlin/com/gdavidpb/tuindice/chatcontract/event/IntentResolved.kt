package com.gdavidpb.tuindice.chatcontract.event

import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The intent [intentId] ended with [outcome]. */
@Serializable
@SerialName("intent_resolved")
public data class IntentResolved(
	override val seq: Long,
	override val turnId: String,
	public val intentId: String,
	public val outcome: OutcomeKind
) : AssistantEvent
