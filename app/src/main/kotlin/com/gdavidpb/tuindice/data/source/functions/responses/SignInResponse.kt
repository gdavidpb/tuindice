package com.gdavidpb.tuindice.data.source.functions.responses

import com.google.gson.annotations.SerializedName

data class SignInResponse(
	@SerializedName("token") val token: String
)
