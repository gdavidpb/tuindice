package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Projection of the grade and outcome of the attempt [attemptId]. [outcome] is the serial name of academiccore's
 * AttemptOutcome, such as "approved".
 */
@Serializable
@SerialName("informe.proyeccion/1")
public data class ProjectionArgs(
	public val mutationId: String,
	public val attemptId: String,
	public val grade: Int? = null,
	public val outcome: String? = null,
	public val clear: Boolean = false
) : IntentArgs, MutatingIntentArgs {
	override val mutationIds: List<String>
		get() = listOf(mutationId)
}
