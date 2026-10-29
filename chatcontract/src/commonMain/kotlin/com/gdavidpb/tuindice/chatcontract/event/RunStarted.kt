package com.gdavidpb.tuindice.chatcontract.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The service started to process the turn. */
@Serializable
@SerialName("run_started")
public data class RunStarted(
	override val seq: Long,
	override val turnId: String
) : AssistantEvent
