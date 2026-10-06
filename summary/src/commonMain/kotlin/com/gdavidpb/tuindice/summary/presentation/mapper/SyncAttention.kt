package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.summary.presentation.model.SyncAttention

fun resolveSyncAttention(
	syncStatus: SyncStatus,
	syncReport: SyncReport
): SyncAttention {
	return when (syncStatus) {
		// Not a problem of the sync, whatever its report says: that failed sync marks the record
		// source unavailable, which must not turn "the university has no record for you yet" into one.
		SyncStatus.NewStudentNoRecord -> SyncAttention.None

		SyncStatus.Unavailable,
		SyncStatus.Failed,
		SyncStatus.OutdatedCredentials,
		SyncStatus.MissingCredentials,
		SyncStatus.RecordAccessDenied -> SyncAttention.Problem

		// A healthy sync is a problem only for the source it could not read. What it did read about
		// the enrollment (not enrolled, annulled) is not the row's to announce.
		SyncStatus.Healthy ->
			if (syncReport.hasUnavailableSource) SyncAttention.Problem else SyncAttention.None
	}
}

// Names the problem the sync row is announcing, or null when it announces none. The halo pulses
// until the user opens the details of the problem with this key, and starts again only when the key
// changes: another status, another source that could not be read.
//
// Built from what the details say and nothing else. The report also carries the instant of the last
// enrollment read, which changes on every sync, and what the university reports about the
// enrollment, which other screens explain: neither must re-arm the halo.
fun resolveSyncAttentionKey(
	syncStatus: SyncStatus,
	syncReport: SyncReport
): String? {
	return when (resolveSyncAttention(syncStatus = syncStatus, syncReport = syncReport)) {
		SyncAttention.None -> null

		SyncAttention.Problem -> listOf(
			syncStatus,
			syncReport.sources.record.status == SyncSourceStatus.Unavailable,
			syncReport.sources.enrollment.status == SyncSourceStatus.Unavailable
		).joinToString(separator = "|")
	}
}
