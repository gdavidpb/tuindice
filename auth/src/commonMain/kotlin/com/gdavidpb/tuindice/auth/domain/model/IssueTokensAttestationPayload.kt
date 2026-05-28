package com.gdavidpb.tuindice.auth.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IssueTokensAttestationPayload(
    @SerialName("usb_id") val usbId: String,
    @SerialName("password") val password: String,
    @SerialName("attested_flow") val attestedFlow: String
)
