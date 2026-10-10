package com.gdavidpb.tuindice.summary.presentation.model

import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncStatus

// What the screen shows of the sync. Its defaults are what the screen assumes until the first
// observation arrives: a healthy account that has never synced and is not syncing.
data class SummarySyncItem(
	val status: SyncStatus = SyncStatus.Healthy,
	val report: SyncReport = SyncReport.success(),
	// Kept as the instant: the text it becomes depends on the day it is read.
	val lastSuccessfulSyncAt: Long? = null,
	val isSyncing: Boolean = false
)
