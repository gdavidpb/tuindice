package com.gdavidpb.tuindice.chatcontract.event

import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.message.ComponentOrFallbackSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A component of the message [messageId] is ready to render. A component this build cannot read keeps the id and the
 * fallback text it carries, the same as a component inside a message does.
 */
@Serializable
@SerialName("component_emitted")
public data class ComponentEmitted(
	override val seq: Long,
	override val turnId: String,
	public val messageId: String,
	@Serializable(with = ComponentOrFallbackSerializer::class)
	public val component: ChatComponent
) : AssistantEvent
