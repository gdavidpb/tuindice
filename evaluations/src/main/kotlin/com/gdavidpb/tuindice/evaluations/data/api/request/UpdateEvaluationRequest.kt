package com.gdavidpb.tuindice.evaluations.data.api.request

import com.google.gson.annotations.SerializedName

data class UpdateEvaluationRequest(
	@SerializedName("evaluation_id") val evaluationId: String,
	@SerializedName("grade") val grade: Double?,
	@SerializedName("max_grade") val maxGrade: Double?,
	@SerializedName("date") val date: Long?,
	@SerializedName("type") val type: Int?
)