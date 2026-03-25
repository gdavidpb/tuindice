package com.gdavidpb.tuindice.evaluations.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddEvaluationResponse(
	@SerialName("mutation_id") val mutationId: String,
	@SerialName("anchor_revision") val anchorRevision: Long,
	@SerialName("evaluation_patch") val evaluationPatch: EvaluationResponse
)
