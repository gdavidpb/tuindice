package com.gdavidpb.tuindice.evaluations.data.api.request

import com.google.gson.annotations.SerializedName

data class AddEvaluationRequest(
	@SerializedName("reference") val reference: String,
	@SerializedName("subject_id") val subjectId: String,
	@SerializedName("type") val type: Int,
	@SerializedName("grade") val grade: Double?,
	@SerializedName("max_grade") val maxGrade: Double,
	@SerializedName("date") val date: Long,
)