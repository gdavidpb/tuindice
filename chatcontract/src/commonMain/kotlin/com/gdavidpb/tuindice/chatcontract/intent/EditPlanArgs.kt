package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Edits the subjects of the plan of the term [termKey]. */
@Serializable
@SerialName("plan.editar/1")
public data class EditPlanArgs(
	public val mutationId: String,
	public val termKey: String,
	public val subjectCodes: List<String>
) : IntentArgs, MutatingIntentArgs {
	override val mutationIds: List<String>
		get() = listOf(mutationId)
}
