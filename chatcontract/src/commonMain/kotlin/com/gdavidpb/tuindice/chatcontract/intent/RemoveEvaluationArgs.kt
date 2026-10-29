package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Removes the evaluation [evaluationId]. */
@Serializable
@SerialName("evaluaciones.borrar/1")
public data class RemoveEvaluationArgs(
	public val mutationId: String,
	public val evaluationId: String
) : IntentArgs, MutatingIntentArgs {
	override val mutationIds: List<String>
		get() = listOf(mutationId)
}
