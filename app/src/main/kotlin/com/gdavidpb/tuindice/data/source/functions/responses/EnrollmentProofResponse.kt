package com.gdavidpb.tuindice.data.source.functions.responses

import com.google.gson.annotations.SerializedName

data class EnrollmentProofResponse(
	@SerializedName("name") val name: String,
	@SerializedName("content") val content: String
)