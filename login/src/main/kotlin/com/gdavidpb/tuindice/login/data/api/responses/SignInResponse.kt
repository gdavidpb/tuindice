package com.gdavidpb.tuindice.login.data.api.responses

import com.google.gson.annotations.SerializedName

data class SignInResponse(
	@SerializedName("token") val token: String
)
