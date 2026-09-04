package com.gdavidpb.tuindice.data.source.sync.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncRecordErrorResponse(
	@SerialName("message") val message: String,
	@SerialName("sync") val sync: SyncReportResponse,
	// A string, not an enum: the server documents that a new reason must never fail a client's
	// parse of an error body. Absent on the servers that predate it.
	@SerialName("reason") val reason: String? = null
)
