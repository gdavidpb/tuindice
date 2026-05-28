package com.gdavidpb.tuindice.evaluations.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetEvaluationResponse(
	@SerialName("evaluation") val evaluation: EvaluationResponse
)
