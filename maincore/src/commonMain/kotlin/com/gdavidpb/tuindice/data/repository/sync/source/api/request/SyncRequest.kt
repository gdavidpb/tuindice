package com.gdavidpb.tuindice.data.repository.sync.source.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncRequest(
	@SerialName("password")
	val password: String
)
