package com.gdavidpb.tuindice.evaluations.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetEvaluationsResponse(
	@SerialName("anchor_revision") val anchorRevision: Long,
	@SerialName("evaluations") val evaluations: List<EvaluationResponse>
)
