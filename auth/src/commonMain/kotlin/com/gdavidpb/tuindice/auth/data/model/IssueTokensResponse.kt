package com.gdavidpb.tuindice.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IssueTokensResponse(
	@SerialName("uid") val uid: String,
	@SerialName("usb_id") val usbId: String,
	@SerialName("access_token") val accessToken: String,
	@SerialName("refresh_token") val refreshToken: String,
	@SerialName("expires_in") val expiresIn: Long
)