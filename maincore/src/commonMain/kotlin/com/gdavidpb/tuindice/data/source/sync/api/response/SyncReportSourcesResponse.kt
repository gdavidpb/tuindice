package com.gdavidpb.tuindice.data.source.sync.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncReportSourcesResponse(
	@SerialName("record") val record: SyncSourceReportResponse,
	@SerialName("enrollment") val enrollment: SyncSourceReportResponse
)
