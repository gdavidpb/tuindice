package com.gdavidpb.tuindice.record.data.repository.quarter.source.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateSubjectRequest(
	@SerialName("subject_id") val subjectId: String,
	@SerialName("grade") val grade: Int
)