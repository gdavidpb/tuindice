package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Progress through the pensum, in credits and in subjects by status. */
@Serializable
@SerialName("pensum.avance/1")
public data class PensumProgressComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val approvedCredits: Int,
	public val totalCredits: Int,
	public val approved: Int,
	public val inProgress: Int,
	public val available: Int,
	public val blocked: Int
) : ChatComponent
