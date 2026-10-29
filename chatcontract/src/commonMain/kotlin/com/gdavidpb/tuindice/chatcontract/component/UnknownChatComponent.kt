package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.Serializable

/** Placeholder for a component type this build does not know. The app shows only its [fallbackText]. */
@Serializable
public data class UnknownChatComponent(
	override val id: String = "",
	override val fallbackText: String = "",
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList()
) : ChatComponent
