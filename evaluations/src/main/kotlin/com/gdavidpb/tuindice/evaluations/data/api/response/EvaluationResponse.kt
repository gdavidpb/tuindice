package com.gdavidpb.tuindice.evaluations.data.api.response

import com.gdavidpb.tuindice.record.data.api.response.SubjectResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EvaluationResponse(
	@SerialName("id") val id: String,
	@SerialName("sid") val sid: String,
	@SerialName("qid") val qid: String,
	@SerialName("grade") val grade: Double?,
	@SerialName("max_grade") val maxGrade: Double,
	@SerialName("date") val date: Long,
	@SerialName("type") val type: Int,
	@SerialName("subject") val subject: SubjectResponse
)