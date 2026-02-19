package com.gdavidpb.tuindice.login.domain.model

import com.gdavidpb.tuindice.base.domain.model.AttestationPayload
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IssueTokensAttestationPayload(
	@SerialName("usb_id") val usbId: String,
	@SerialName("password") val password: String
) : AttestationPayload()