package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Asks the user to accept the assistant terms and privacy policy, at consent [version]. */
@Serializable
@SerialName("sistema.consentimiento/1")
public data class ConsentComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.SHEET,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val version: Int,
	public val termsUrl: String,
	public val privacyUrl: String
) : ChatComponent
