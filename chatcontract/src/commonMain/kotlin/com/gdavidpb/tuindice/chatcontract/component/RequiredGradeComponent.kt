package com.gdavidpb.tuindice.chatcontract.component

import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Points needed in a subject to reach a target grade. */
@Serializable
@SerialName("evaluaciones.meta/1")
public data class RequiredGradeComponent(
	override val id: String,
	override val fallbackText: String,
	override val display: ComponentDisplay = ComponentDisplay.INLINE,
	override val entityRefs: List<EntityRef> = emptyList(),
	public val subjectCode: String,
	public val subjectName: String,
	public val targetGrade: Int,
	public val requiredPoints: Double?,
	public val remainingPoints: Double,
	public val feasible: Boolean
) : ChatComponent
