package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Points the user to support: each of the [lines] is a phone number, a link, or both. */
@Serializable
@SerialName("sistema.apoyo/1")
public data class SupportComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val lines: List<SupportLine>
) : ChatComponent
