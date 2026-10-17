package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Edits the evaluation [evaluationId]. */
@Serializable
@SerialName("evaluaciones.editar/1")
public data class EditEvaluationArgs(
	public val mutationId: String,
	public val evaluationId: String,
	public val grade: Double? = null,
	public val maxGrade: Double? = null,
	public val dateMillis: Long? = null,
	public val evaluationType: String? = null,
	public val scheduleMode: String? = null
) : IntentArgs, MutatingIntentArgs {
	override val mutationIds: List<String>
		get() = listOf(mutationId)
}
