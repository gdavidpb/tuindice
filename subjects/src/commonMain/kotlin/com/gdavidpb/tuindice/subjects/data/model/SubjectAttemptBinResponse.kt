package com.gdavidpb.tuindice.subjects.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubjectAttemptBinResponse(
	@SerialName("bucket") val bucket: String,
	@SerialName("count") val count: Int
)
