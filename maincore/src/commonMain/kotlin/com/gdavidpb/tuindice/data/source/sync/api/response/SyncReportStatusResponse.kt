package com.gdavidpb.tuindice.data.source.sync.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SyncReportStatusResponse {
	@SerialName("success")
	Success,

	@SerialName("partial")
	Partial,

	@SerialName("failed")
	Failed
}
