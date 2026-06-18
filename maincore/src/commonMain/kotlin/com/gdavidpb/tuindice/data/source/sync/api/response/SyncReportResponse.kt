package com.gdavidpb.tuindice.data.source.sync.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncReportResponse(
	@SerialName("status") val status: SyncReportStatusResponse,
	@SerialName("sources") val sources: SyncReportSourcesResponse
)
