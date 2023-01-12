package com.gdavidpb.tuindice.enrollmentproof.data.api.responses

import com.google.gson.annotations.SerializedName

data class EnrollmentProofResponse(
	@SerializedName("name") val name: String,
	@SerializedName("content") val content: String
)