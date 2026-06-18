package com.gdavidpb.tuindice.base.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SyncReportStatus {
	@SerialName("success")
	Success,

	@SerialName("partial")
	Partial,

	@SerialName("failed")
	Failed
}
