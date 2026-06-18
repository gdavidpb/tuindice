package com.gdavidpb.tuindice.data.source.sync.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SyncSourceStatusResponse {
	@SerialName("success")
	Success,

	@SerialName("unavailable")
	Unavailable,

	@SerialName("not_attempted")
	NotAttempted
}
