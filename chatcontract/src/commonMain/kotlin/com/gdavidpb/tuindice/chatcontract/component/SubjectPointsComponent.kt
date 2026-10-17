package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Evaluation points of each subject. */
@Serializable
@SerialName("evaluaciones.puntos/1")
public data class SubjectPointsComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val rows: List<SubjectPointsRow>
) : ChatComponent
