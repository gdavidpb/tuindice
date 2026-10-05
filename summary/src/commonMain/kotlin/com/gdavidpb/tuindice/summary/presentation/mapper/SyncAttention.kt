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
	val isInformative = syncStatus == SyncStatus.NewStudentNoRecord ||
		syncReport.sources.enrollment.status == SyncSourceStatus.NotEnrolled ||
		syncReport.sources.enrollment.situation != null

	return when {
		isProblem || syncReport.hasUnavailableSource -> SyncAttention.Problem
		isInformative -> SyncAttention.Informative
		else -> SyncAttention.None
	}
}
