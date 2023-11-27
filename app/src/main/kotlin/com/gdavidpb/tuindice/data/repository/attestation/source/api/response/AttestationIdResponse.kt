package com.gdavidpb.tuindice.data.repository.attestation.source.api.response

import com.google.gson.annotations.SerializedName

data class AttestationIdResponse(
	@SerializedName("id") val id: String
)