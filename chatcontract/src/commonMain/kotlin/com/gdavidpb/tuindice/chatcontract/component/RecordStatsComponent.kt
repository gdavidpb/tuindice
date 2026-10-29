package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Counts of the academic record and its grade histogram, one count per grade from 1 to 5. */
@Serializable
@SerialName("informe.estadisticas/1")
public data class RecordStatsComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val approved: Int,
	public val failed: Int,
	public val retired: Int,
	public val approvedFirstTry: Int,
	public val repeated: Int,
	public val histogram: List<Int>
) : ChatComponent
