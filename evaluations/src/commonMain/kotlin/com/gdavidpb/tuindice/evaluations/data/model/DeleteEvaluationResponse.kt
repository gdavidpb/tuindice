package com.gdavidpb.tuindice.evaluations.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteEvaluationResponse(
	@SerialName("mutation_id") val mutationId: String,
	@SerialName("removed_evaluation_id") val removedEvaluationId: String
)
