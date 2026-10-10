package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.presentation.mapper.NewStudentNoRecordTexts
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryFailedItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryFailedKind
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_failed_message
import tuindice.summary.generated.resources.summary_failed_title

// Why Summary has nothing to show decides what it says: an account the university has no record
// for yet reads the shared new-student copy; anything else is the plain failure.
fun resolveSummaryFailedItem(syncStatus: SyncStatus): SummaryFailedItem {
	return when (syncStatus) {
		SyncStatus.NewStudentNoRecord -> SummaryFailedItem(
			kind = SummaryFailedKind.NewStudentNoRecord,
			title = NewStudentNoRecordTexts.title,
			message = NewStudentNoRecordTexts.message
		)

		SyncStatus.Healthy,
		SyncStatus.Unavailable,
		SyncStatus.Failed,
		SyncStatus.OutdatedCredentials,
		SyncStatus.MissingCredentials,
		SyncStatus.RecordAccessDenied -> SummaryFailedItem(
			kind = SummaryFailedKind.Error,
			title = UiText.Resource(Res.string.summary_failed_title),
			message = UiText.Resource(Res.string.summary_failed_message)
		)
	}
}
