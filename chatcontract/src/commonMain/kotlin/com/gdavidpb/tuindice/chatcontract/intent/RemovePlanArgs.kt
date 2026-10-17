package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Removes the plan of the term [termKey]. */
@Serializable
@SerialName("plan.borrar/1")
public data class RemovePlanArgs(
	public val mutationId: String,
	public val termKey: String
) : IntentArgs, MutatingIntentArgs {
	override val mutationIds: List<String>
		get() = listOf(mutationId)
}
