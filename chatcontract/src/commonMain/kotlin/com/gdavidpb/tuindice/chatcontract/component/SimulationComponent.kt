package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Index projection of a term, with the grades that were simulated. */
@Serializable
@SerialName("informe.simulacion/1")
public data class SimulationComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val termId: String,
	public val termIndex: Double?,
	public val cumulativeIndex: Double?,
	public val changes: List<SimulatedGrade>
) : ChatComponent
