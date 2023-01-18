package com.gdavidpb.tuindice.evaluations.data.api.request

import com.google.gson.annotations.SerializedName

data class EvaluationBody(
	@SerializedName("sid") val sid: String,
	@SerializedName("qid") val qid: String,
	@SerializedName("notes") val notes: String,
	@SerializedName("grade") val grade: Double,
	@SerializedName("maxGrade") val maxGrade: Double,
	@SerializedName("date") val date: Long,
	@SerializedName("type") val type: Int,
	@SerializedName("is_done") val isDone: Boolean
)