package com.gdavidpb.tuindice.chatcontract.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The service is running [tool]; [label] is the text to show while it does. */
@Serializable
@SerialName("progress")
public data class Progress(
	override val seq: Long,
	override val turnId: String,
	public val tool: String,
	public val label: String
) : AssistantEvent
