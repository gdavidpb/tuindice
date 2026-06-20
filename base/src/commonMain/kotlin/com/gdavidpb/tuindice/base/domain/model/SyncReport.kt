package com.gdavidpb.tuindice.base.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncReport(
	@SerialName("status") val status: SyncReportStatus,
	@SerialName("sources") val sources: SyncReportSources
) {
	val hasUnavailableSource: Boolean
		get() = sources.hasUnavailableSource

	companion object {
		fun success(): SyncReport {
			return SyncReport(
				status = SyncReportStatus.Success,
				sources = SyncReportSources(
					record = SyncSourceReport(SyncSourceStatus.Success),
					enrollment = SyncSourceReport(SyncSourceStatus.Success)
				)
			)
		}

		fun partialEnrollmentUnavailable(): SyncReport {
			return SyncReport(
				status = SyncReportStatus.Partial,
				sources = SyncReportSources(
					record = SyncSourceReport(SyncSourceStatus.Success),
					enrollment = SyncSourceReport(SyncSourceStatus.Unavailable)
				)
			)
		}

		fun failedRecordUnavailable(): SyncReport {
			return SyncReport(
				status = SyncReportStatus.Failed,
				sources = SyncReportSources(
					record = SyncSourceReport(SyncSourceStatus.Unavailable),
					enrollment = SyncSourceReport(SyncSourceStatus.NotAttempted)
				)
			)
		}
	}
}
