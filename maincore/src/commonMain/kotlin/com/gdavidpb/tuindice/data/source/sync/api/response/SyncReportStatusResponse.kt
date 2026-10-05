package com.gdavidpb.tuindice.data.source.sync.api.response

import kotlinx.serialization.Serializable

// Same tolerance as [SyncSourceStatusResponse]: an unknown value decodes to [Unknown].
@Serializable(with = SyncReportStatusResponseSerializer::class)
enum class SyncReportStatusResponse(val wireName: String?) {
	Success("success"),
	Partial("partial"),
	Failed("failed"),
	Unknown(null)
}
