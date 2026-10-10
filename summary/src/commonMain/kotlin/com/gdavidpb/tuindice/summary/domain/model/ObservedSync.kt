package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncStatus

// What the sync says of the account right now, read in one observation so the status, the report
// that explains it and whether another sync is already running never come from different moments.
data class ObservedSync(
	val status: SyncStatus,
	val report: SyncReport,
	val lastSuccessfulSyncAt: Long?,
	val isInProgress: Boolean
)
