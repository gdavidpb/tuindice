package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Profile summary: career, pensum, cumulative index and credit progress. */
@Serializable
@SerialName("perfil.resumen/1")
public data class ProfileSummaryComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val careerCode: String,
	public val careerName: String,
	public val pensumLabel: String,
	public val cumulativeIndex: Double?,
	public val approvedCredits: Int,
	public val totalCredits: Int?,
	public val currentTermLabel: String?
) : ChatComponent
