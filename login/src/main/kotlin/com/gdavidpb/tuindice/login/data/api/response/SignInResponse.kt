package com.gdavidpb.tuindice.login.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignInResponse(
	@SerialName("token") val token: String
)