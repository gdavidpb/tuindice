package com.gdavidpb.tuindice.login.data.repository.login.source.api.attestation

import com.gdavidpb.tuindice.base.domain.model.attestation.AttestationPayload
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignInAttestationPayload(
	@SerialName("basicToken") val basicToken: String,
	@SerialName("refreshToken") val refreshToken: Boolean
) : AttestationPayload
