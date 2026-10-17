package com.gdavidpb.tuindice.chatcontract.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The service finished the turn; [usage] is the quota left after it, when the service reports it. */
@Serializable
@SerialName("run_finished")
public data class RunFinished(
	override val seq: Long,
	override val turnId: String,
	public val usage: UsageSummary? = null
) : AssistantEvent
