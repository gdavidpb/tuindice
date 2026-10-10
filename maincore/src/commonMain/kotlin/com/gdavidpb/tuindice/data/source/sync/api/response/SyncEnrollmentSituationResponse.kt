package com.gdavidpb.tuindice.data.source.sync.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Defaults are mandatory: the server omits whatever equals its default (`encodeDefaults = false`).
@Serializable
data class SyncEnrollmentSituationResponse(
	@SerialName("code") val code: String? = null,
	@SerialName("description") val description: String? = null
)
