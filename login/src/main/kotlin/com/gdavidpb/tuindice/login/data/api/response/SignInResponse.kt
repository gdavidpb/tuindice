package com.gdavidpb.tuindice.login.data.api.response

import com.google.gson.annotations.SerializedName

data class SignInResponse(
	@SerializedName("token") val token: String
)
