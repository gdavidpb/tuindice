package com.gdavidpb.tuindice.evaluations.data.api.request

import com.google.gson.annotations.SerializedName

data class RemoveEvaluationRequest(
	@SerializedName("evaluation_id") val evaluationId: String
)