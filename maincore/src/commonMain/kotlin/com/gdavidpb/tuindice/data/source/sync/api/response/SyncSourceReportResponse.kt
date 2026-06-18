package com.gdavidpb.tuindice.data.source.sync.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncSourceReportResponse(
	@SerialName("status") val status: SyncSourceStatusResponse
)
