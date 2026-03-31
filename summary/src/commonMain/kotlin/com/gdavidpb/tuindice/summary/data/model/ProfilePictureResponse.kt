package com.gdavidpb.tuindice.summary.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfilePictureResponse(
	@SerialName("url") val url: String
)
