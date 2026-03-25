package com.gdavidpb.tuindice.evaluations.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetEvaluationResponse(
	@SerialName("anchor_revision") val anchorRevision: Long,
	@SerialName("evaluation") val evaluation: EvaluationResponse
)
