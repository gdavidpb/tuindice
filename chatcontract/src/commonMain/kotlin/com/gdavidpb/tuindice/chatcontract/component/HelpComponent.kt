package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Step-by-step help, with the screen to open when there is one. */
@Serializable
@SerialName("conocimiento.ayuda/1")
public data class HelpComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val title: String,
	public val steps: List<String>,
	public val target: NavTarget? = null
) : ChatComponent
