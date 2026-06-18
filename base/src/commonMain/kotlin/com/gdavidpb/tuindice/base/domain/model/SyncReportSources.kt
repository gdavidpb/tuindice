package com.gdavidpb.tuindice.base.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncReportSources(
	@SerialName("record") val record: SyncSourceReport,
	@SerialName("enrollment") val enrollment: SyncSourceReport
) {
	val hasUnavailableSource: Boolean
		get() = record.status == SyncSourceStatus.Unavailable ||
			enrollment.status == SyncSourceStatus.Unavailable
}
