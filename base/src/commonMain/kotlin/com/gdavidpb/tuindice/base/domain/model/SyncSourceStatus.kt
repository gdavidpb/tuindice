package com.gdavidpb.tuindice.base.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SyncSourceStatus {
	@SerialName("success")
	Success,

	@SerialName("unavailable")
	Unavailable,

	@SerialName("not_attempted")
	NotAttempted
}
