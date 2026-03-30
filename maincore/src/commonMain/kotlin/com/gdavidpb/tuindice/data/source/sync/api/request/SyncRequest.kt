package com.gdavidpb.tuindice.data.source.sync.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncRequest(
	@SerialName("password")
	val password: String
)
