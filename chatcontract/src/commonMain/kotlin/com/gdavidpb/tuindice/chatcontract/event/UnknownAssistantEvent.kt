package com.gdavidpb.tuindice.chatcontract.event

import kotlinx.serialization.Serializable

/** Placeholder for an event type this build does not know. It keeps [seq] and [turnId] and nothing else. */
@Serializable
public data class UnknownAssistantEvent(
	override val seq: Long = -1,
	override val turnId: String = ""
) : AssistantEvent
