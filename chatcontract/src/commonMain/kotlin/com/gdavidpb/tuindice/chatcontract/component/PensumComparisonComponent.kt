package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Comparison of the pensum with the pensum of another career. */
@Serializable
@SerialName("pensum.comparacion/1")
public data class PensumComparisonComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val otherCareerName: String,
	public val otherPensumLabel: String,
	public val sharedCredits: Int,
	public val missingSubjects: List<SubjectRef>,
	public val notes: List<String> = emptyList()
) : ChatComponent
