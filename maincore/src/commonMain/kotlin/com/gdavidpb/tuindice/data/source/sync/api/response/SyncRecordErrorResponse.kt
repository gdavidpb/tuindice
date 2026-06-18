package com.gdavidpb.tuindice.data.source.sync.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncRecordErrorResponse(
	@SerialName("message") val message: String,
	@SerialName("sync") val sync: SyncReportResponse
)
