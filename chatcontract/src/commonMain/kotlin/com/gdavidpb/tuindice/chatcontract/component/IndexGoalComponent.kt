package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Cumulative index target: the term index it requires, and whether it is feasible. */
@Serializable
@SerialName("informe.meta/1")
public data class IndexGoalComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val targetCumulative: Double,
	public val credits: Int,
	public val requiredTermIndex: Double?,
	public val feasible: Boolean
) : ChatComponent
