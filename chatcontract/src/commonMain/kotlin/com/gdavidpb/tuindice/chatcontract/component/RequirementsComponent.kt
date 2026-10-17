package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Prerequisites and corequisites of a subject, and the subjects it unlocks. */
@Serializable
@SerialName("pensum.requisitos/1")
public data class RequirementsComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val subjectCode: String,
	public val subjectName: String,
	public val prerequisites: List<SubjectRef>,
	public val corequisites: List<SubjectRef>,
	public val unlocks: List<SubjectRef>
) : ChatComponent
