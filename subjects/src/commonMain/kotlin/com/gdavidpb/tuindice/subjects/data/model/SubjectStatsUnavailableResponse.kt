package com.gdavidpb.tuindice.subjects.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubjectStatsUnavailableResponse(
	@SerialName("error_code") val errorCode: String,
	@SerialName("subject_code") val subjectCode: String,
	@SerialName("expires_at") val expiresAt: Long
)
