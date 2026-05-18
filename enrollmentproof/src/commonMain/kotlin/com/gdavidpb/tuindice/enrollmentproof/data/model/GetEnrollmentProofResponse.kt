package com.gdavidpb.tuindice.enrollmentproof.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable

data class GetEnrollmentProofResponse(
	@SerialName("name") val name: String,
	@SerialName("content") val content: String
)