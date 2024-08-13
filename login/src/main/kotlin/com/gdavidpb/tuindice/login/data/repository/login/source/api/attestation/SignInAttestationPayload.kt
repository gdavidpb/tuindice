package com.gdavidpb.tuindice.login.data.repository.login.source.api.attestation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignInAttestationPayload(
	@SerialName("usb_id") val usbId: String,
	@SerialName("password") val password: String
)