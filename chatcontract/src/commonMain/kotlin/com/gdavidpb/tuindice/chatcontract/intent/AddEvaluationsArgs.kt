package com.gdavidpb.tuindice.chatcontract.intent

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Adds the evaluations in [items]. [source] tells whether the user typed them or took them from a photo. */
@Serializable
@SerialName("evaluaciones.agregar/1")
public data class AddEvaluationsArgs(
	public val items: List<EvaluationDraft>,
	public val source: EvaluationDraftSource = EvaluationDraftSource.MANUAL
) : IntentArgs, MutatingIntentArgs {
	override val mutationIds: List<String>
		get() = items.map { it.mutationId }
}
