package com.gdavidpb.tuindice.record.data.source.api.response

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
	@SerialName("credits_sum") val creditsSum: Int,
	@SerialName("is_current") val isCurrent: Boolean,
	@SerialName("is_read_only") val isReadOnly: Boolean,
	@SerialName("revision") val revision: Long,
	@SerialName("subjects") val subjects: List<SubjectResponse>
)
