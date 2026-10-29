package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.chatcontract.event.UsageSummary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Shows the requests left in [usage], and when the quota resets if the service knows it. */
@Serializable
@SerialName("sistema.aviso_uso/1")
public data class UsageNoticeComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val usage: UsageSummary,
	public val resetsAtMillis: Long? = null
) : ChatComponent
