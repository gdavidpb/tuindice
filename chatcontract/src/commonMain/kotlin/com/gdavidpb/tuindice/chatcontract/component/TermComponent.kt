package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Report of one term: its indexes, its credits and its subject rows. */
@Serializable
@SerialName("informe.trimestre/1")
public data class TermComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val termId: String,
	public val termLabel: String,
	public val termIndex: Double?,
	public val cumulativeIndex: Double?,
	public val credits: Int,
	public val rows: List<TermSubjectRow>
) : ChatComponent
