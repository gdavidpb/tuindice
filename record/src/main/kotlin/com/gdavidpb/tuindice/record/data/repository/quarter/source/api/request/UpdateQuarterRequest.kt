package com.gdavidpb.tuindice.record.data.repository.quarter.source.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateQuarterRequest(
	@SerialName("quarter_id") val quarterId: String,
	@SerialName("subjects_updates") val subjectsUpdates: List<UpdateSubjectRequest>
)