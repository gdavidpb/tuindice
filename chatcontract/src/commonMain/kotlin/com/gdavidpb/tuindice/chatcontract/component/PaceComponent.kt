package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Credit pace: credits per term, their average, and an optional estimate of the terms left. */
@Serializable
@SerialName("informe.ritmo/1")
public data class PaceComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val creditsPerTerm: List<TermCredits>,
	public val averageCredits: Double,
	public val remainingTermsEstimate: Int? = null
) : ChatComponent
