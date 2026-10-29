package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Grade statistics of a subject within a scope. */
@Serializable
@SerialName("materias.estadisticas/1")
public data class SubjectStatsComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val subjectCode: String,
	public val subjectName: String,
	public val scope: String,
	public val average: Double?,
	public val median: Double?,
	public val passRate: Double?,
	public val difficulty: String?,
	public val histogram: List<Int>
) : ChatComponent
