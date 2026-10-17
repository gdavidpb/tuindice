package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Button that opens [target] in the app; [label] is its text. */
@Serializable
@SerialName("nav.abrir/1")
public data class OpenScreenComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val target: NavTarget,
	public val label: String
) : ChatComponent
