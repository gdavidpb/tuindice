package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Creates the plan of a term, given by [periodYear] and [periodCode], with the subjects [subjectCodes]. */
@Serializable
@SerialName("plan.crear/1")
public data class CreatePlanArgs(
	public val mutationId: String,
	public val periodYear: Int,
	public val periodCode: String,
	public val subjectCodes: List<String>
) : IntentArgs, MutatingIntentArgs {
	override val mutationIds: List<String>
		get() = listOf(mutationId)
}
