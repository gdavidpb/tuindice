package com.gdavidpb.tuindice.data.source.sync.api.mapper

import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncReportSources
import com.gdavidpb.tuindice.base.domain.model.SyncReportStatus
import com.gdavidpb.tuindice.base.domain.model.SyncSourceReport
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.data.source.sync.api.response.SyncReportResponse
import com.gdavidpb.tuindice.data.source.sync.api.response.SyncReportSourcesResponse
import com.gdavidpb.tuindice.data.source.sync.api.response.SyncReportStatusResponse
import com.gdavidpb.tuindice.data.source.sync.api.response.SyncSourceReportResponse
import com.gdavidpb.tuindice.data.source.sync.api.response.SyncSourceStatusResponse

fun SyncReportResponse.toSyncReport(): SyncReport {
	return SyncReport(
		status = status.toSyncReportStatus(),
		sources = sources.toSyncReportSources()
	)
}

private fun SyncReportStatusResponse.toSyncReportStatus(): SyncReportStatus {
	return when (this) {
		SyncReportStatusResponse.Success -> SyncReportStatus.Success
		SyncReportStatusResponse.Partial -> SyncReportStatus.Partial
		SyncReportStatusResponse.Failed -> SyncReportStatus.Failed
	}
}

private fun SyncReportSourcesResponse.toSyncReportSources(): SyncReportSources {
	return SyncReportSources(
		record = record.toSyncSourceReport(),
		enrollment = enrollment.toSyncSourceReport()
	)
}

private fun SyncSourceReportResponse.toSyncSourceReport(): SyncSourceReport {
	return SyncSourceReport(
		status = status.toSyncSourceStatus()
	)
}

private fun SyncSourceStatusResponse.toSyncSourceStatus(): SyncSourceStatus {
	return when (this) {
		SyncSourceStatusResponse.Success -> SyncSourceStatus.Success
		SyncSourceStatusResponse.Unavailable -> SyncSourceStatus.Unavailable
		SyncSourceStatusResponse.NotAttempted -> SyncSourceStatus.NotAttempted
	}
}
