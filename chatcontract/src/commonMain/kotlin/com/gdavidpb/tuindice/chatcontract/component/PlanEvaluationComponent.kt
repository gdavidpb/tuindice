package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Evaluation of a term plan: load, clashes and warnings. */
@Serializable
@SerialName("plan.evaluacion/1")
public data class PlanEvaluationComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val termLabel: String,
	public val credits: Int,
	public val loadBand: String,
	public val loadIndex: Double?,
	public val clashes: Int,
	public val pendingRequirements: List<String> = emptyList(),
	public val slotWarnings: List<String> = emptyList()
) : ChatComponent
