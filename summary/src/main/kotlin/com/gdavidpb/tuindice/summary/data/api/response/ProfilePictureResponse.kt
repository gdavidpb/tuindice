package com.gdavidpb.tuindice.summary.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfilePictureResponse(
	@SerialName("uid") val uid: String,
	@SerialName("url") val url: String
)