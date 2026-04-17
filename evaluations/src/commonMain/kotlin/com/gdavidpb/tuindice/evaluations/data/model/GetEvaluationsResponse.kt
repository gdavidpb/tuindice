package com.gdavidpb.tuindice.evaluations.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetEvaluationsResponse(
	@SerialName("evaluations") val evaluations: List<EvaluationResponse>
)
