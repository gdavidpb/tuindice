package com.gdavidpb.tuindice.evaluations.data.api.response

import com.gdavidpb.tuindice.record.data.api.response.SubjectResponse
import com.google.gson.annotations.SerializedName

data class EvaluationResponse(
	@SerializedName("id") val id: String,
	@SerializedName("sid") val sid: String,
	@SerializedName("qid") val qid: String,
	@SerializedName("ordinal") val ordinal: Int,
	@SerializedName("grade") val grade: Double,
	@SerializedName("max_grade") val maxGrade: Double,
	@SerializedName("date") val date: Long,
	@SerializedName("last_modified") val lastModified: Long,
	@SerializedName("type") val type: Int,
	@SerializedName("is_completed") val isCompleted: Boolean,
	@SerializedName("subject") val subject: SubjectResponse
)