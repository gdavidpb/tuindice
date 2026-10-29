package com.gdavidpb.tuindice.chatcontract.message

import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A component rendered inside the message. */
@Serializable
@SerialName("component")
public data class Component(
	@Serializable(with = ComponentOrFallbackSerializer::class)
	public val component: ChatComponent
) : MessagePart
