package com.gdavidpb.tuindice.security.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val ATTESTATION_KEY_USER_MISMATCH_CODE = "attestation_key_user_mismatch"

@Serializable
data class AttestationKeyUserMismatchResponse(
	@SerialName("code") val code: String
)
