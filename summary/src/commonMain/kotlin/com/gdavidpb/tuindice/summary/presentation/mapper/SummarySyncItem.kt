package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.summary.domain.model.ObservedSync
import com.gdavidpb.tuindice.summary.presentation.model.SummarySyncItem

fun ObservedSync.toSummarySyncItem(): SummarySyncItem {
	return SummarySyncItem(
		status = status,
		report = report,
		lastSuccessfulSyncAt = lastSuccessfulSyncAt,
		isSyncing = isInProgress
	)
}
