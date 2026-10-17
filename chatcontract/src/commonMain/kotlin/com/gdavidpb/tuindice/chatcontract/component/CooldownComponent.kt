package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.abuse.AbuseReasonCode
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Pauses the assistant for the user until [untilMillis]. [appealable] tells whether the user may appeal it. */
@Serializable
@SerialName("sistema.enfriamiento/1")
public data class CooldownComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val reason: AbuseReasonCode,
	public val untilMillis: Long,
	public val appealable: Boolean
) : ChatComponent
