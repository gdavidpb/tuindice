package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Asks the user whether the remembered [statement], stored as [memoryId], still applies. */
@Serializable
@SerialName("sistema.sigue_aplicando/1")
public data class StillAppliesComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val memoryId: String,
	public val statement: String
) : ChatComponent
