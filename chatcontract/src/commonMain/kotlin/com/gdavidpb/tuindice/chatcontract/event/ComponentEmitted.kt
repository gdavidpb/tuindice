package com.gdavidpb.tuindice.chatcontract.event

import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A component of the message [messageId] is ready to render. */
@Serializable
@SerialName("component_emitted")
public data class ComponentEmitted(
	override val seq: Long,
	override val turnId: String,
	public val messageId: String,
	public val component: ChatComponent
) : AssistantEvent
