package com.gdavidpb.tuindice.enrollmentproof.data.api.response

import com.google.gson.annotations.SerializedName

data class EnrollmentProofResponse(
	@SerializedName("name") val name: String,
	@SerializedName("content") val content: String
)