package com.gdavidpb.tuindice.login.domain.model

import com.gdavidpb.tuindice.base.domain.model.AttestationPayload
import kotlinx.serialization.Serializable

@Serializable
data class SignInAttestationPayload(
	val usbId: String,
	val password: String
) : AttestationPayload()
