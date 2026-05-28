package com.gdavidpb.tuindice.enrollmentproof.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FetchEnrollmentProofRequest(
	@SerialName("password") val password: String
)
