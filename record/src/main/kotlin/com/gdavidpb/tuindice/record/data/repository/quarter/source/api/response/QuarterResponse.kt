package com.gdavidpb.tuindice.record.data.repository.quarter.source.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuarterResponse(
	@SerialName("id") val id: String,
	@SerialName("name") val name: String,
	@SerialName("start_date") val startDate: Long,
	@SerialName("end_date") val endDate: Long,
	@SerialName("grade") val grade: Double,
	@SerialName("grade_sum") val gradeSum: Double,
	@SerialName("credits") val credits: Int,
	@SerialName("status") val status: Int,
	@SerialName("subjects") val subjects: List<SubjectResponse>
)