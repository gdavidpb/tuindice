package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.abuse.AbuseReasonCode
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Suspends the assistant for the user, until [untilMillis] if set. [appealable] tells if it can be appealed. */
@Serializable
@SerialName("sistema.suspension/1")
public data class SuspensionComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val reason: AbuseReasonCode,
	public val untilMillis: Long? = null,
	public val appealable: Boolean
) : ChatComponent
