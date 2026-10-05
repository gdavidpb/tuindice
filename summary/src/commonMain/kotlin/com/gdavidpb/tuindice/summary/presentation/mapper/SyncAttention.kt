package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.summary.presentation.model.SyncAttention

fun resolveSyncAttention(
	syncStatus: SyncStatus,
	syncReport: SyncReport
): SyncAttention {
	val isProblem = when (syncStatus) {
		SyncStatus.Unavailable,
		SyncStatus.Failed,
		SyncStatus.OutdatedCredentials,
		SyncStatus.MissingCredentials,
		SyncStatus.RecordAccessDenied -> true

		SyncStatus.Healthy,
		SyncStatus.NewStudentNoRecord -> false
	}
	val isInformative = syncReport.sources.enrollment.status == SyncSourceStatus.NotEnrolled ||
		syncReport.sources.enrollment.situation != null

	return when {
		// Checked first: the report of that failed sync marks the record source unavailable, which
		// must not turn "the university has no record for you yet" into a problem.
		syncStatus == SyncStatus.NewStudentNoRecord -> SyncAttention.Informative
		isProblem || syncReport.hasUnavailableSource -> SyncAttention.Problem
		isInformative -> SyncAttention.Informative
		else -> SyncAttention.None
	}
}

// Names what the sync row is announcing, or null when it announces nothing. The halo pulses until
// the user opens the details of the announcement with this key, and starts again only when the key
// changes: a provisional annulment that became final, information that became a problem.
//
// Built field by field: the report also carries the instant of the last enrollment read, which
// changes on every sync and must not re-arm the halo.
fun resolveSyncAttentionKey(
	syncStatus: SyncStatus,
	syncReport: SyncReport,
	hasCurrentTerm: Boolean
): String? {
	val syncAttention = resolveSyncAttention(syncStatus = syncStatus, syncReport = syncReport)
	val enrollment = syncReport.sources.enrollment
	// The moment of an annulment is part of what is read (provisional while the record keeps a
	// current term, final once it does not). Only an informative row shows it, and without an
	// annulment the current term says nothing here.
	val annulmentMoment = enrollment.situation
		?.takeIf { syncAttention == SyncAttention.Informative }
		?.let { if (hasCurrentTerm) ANNULMENT_PROVISIONAL else ANNULMENT_FINAL }

	return when (syncAttention) {
		SyncAttention.None -> null

		SyncAttention.Problem,
		SyncAttention.Informative -> listOf(
			syncAttention,
			syncStatus,
			syncReport.status,
			syncReport.sources.record.status,
			enrollment.status,
			enrollment.situation?.annulmentCause,
			annulmentMoment
		).joinToString(separator = "|")
	}
}

private const val ANNULMENT_PROVISIONAL = "provisional"
private const val ANNULMENT_FINAL = "final"
