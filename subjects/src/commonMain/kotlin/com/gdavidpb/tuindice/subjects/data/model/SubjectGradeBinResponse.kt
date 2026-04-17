package com.gdavidpb.tuindice.subjects.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubjectGradeBinResponse(
	@SerialName("grade") val grade: Int,
	@SerialName("count") val count: Int
)
