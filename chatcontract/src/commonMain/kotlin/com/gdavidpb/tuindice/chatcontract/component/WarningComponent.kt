package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.abuse.AbuseReasonCode
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Warns the user about [reason]. */
@Serializable
@SerialName("sistema.advertencia/1")
public data class WarningComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val reason: AbuseReasonCode
) : ChatComponent
