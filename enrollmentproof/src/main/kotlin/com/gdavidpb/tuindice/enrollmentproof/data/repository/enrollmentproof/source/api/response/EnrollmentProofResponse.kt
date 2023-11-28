package com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.source.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable

data class EnrollmentProofResponse(
	@SerialName("name") val name: String,
	@SerialName("content") val content: String
)