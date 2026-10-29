package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Index over the terms, with the best and the worst term marked by their ids. */
@Serializable
@SerialName("informe.serie/1")
public data class IndexSeriesComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val points: List<IndexPoint>,
	public val bestTermId: String? = null,
	public val worstTermId: String? = null
) : ChatComponent
