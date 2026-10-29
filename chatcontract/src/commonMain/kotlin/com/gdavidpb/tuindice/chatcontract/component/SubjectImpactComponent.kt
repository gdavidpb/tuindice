package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Chain of subjects that depend on a subject, and how many terms it spans. */
@Serializable
@SerialName("pensum.impacto/1")
public data class SubjectImpactComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val subjectCode: String,
	public val subjectName: String,
	public val chain: List<SubjectRef>,
	public val lengthInTerms: Int
) : ChatComponent
