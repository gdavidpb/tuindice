package com.gdavidpb.tuindice.record.data.repository.quarter.source.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddQuarterRequest(
	@SerialName("reference") val reference: String,
	@SerialName("start_date") val startDate: Long,
	@SerialName("end_date") val endDate: Long,
	@SerialName("subjects") val subjects: List<AddSubjectRequest>
)