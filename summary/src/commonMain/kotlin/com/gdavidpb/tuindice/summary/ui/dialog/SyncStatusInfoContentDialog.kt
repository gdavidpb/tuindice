package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.summary.presentation.mapper.resolveUnavailableSourcesMessage
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.dialog_button_understood
import tuindice.summary.generated.resources.dialog_message_record_access_denied
import tuindice.summary.generated.resources.dialog_message_sync_failed
import tuindice.summary.generated.resources.dialog_message_sync_unavailable
import tuindice.summary.generated.resources.dialog_title_record_access_denied
import tuindice.summary.generated.resources.dialog_title_sync_failed
import tuindice.summary.generated.resources.dialog_title_sync_sources_unavailable
import tuindice.summary.generated.resources.dialog_title_sync_unavailable

// Explains the problem the sync row is announcing, and only a problem: what the university reports
// about the enrollment or the record is explained by the screens that own it.
@Composable
fun SyncStatusInfoContentDialog(
	syncStatus: SyncStatus,
	syncReport: SyncReport = SyncReport.success(),
	onUpdatePasswordClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	// A failed sync always reports its sources as unavailable, so that alone cannot pick the dialog.
	if (!syncStatus.ignoresUnavailableSources && syncReport.hasUnavailableSource) {
		SyncStatusInfoDialog(
			titleText = stringResource(Res.string.dialog_title_sync_sources_unavailable),
			messageText = resolveUnavailableSourcesMessage(syncReport = syncReport).asString(),
			confirmText = stringResource(Res.string.dialog_button_understood),
			onConfirmClick = {},
			onDismissRequest = onDismissRequest
		)
		return
	}

	when (syncStatus) {
		// Nothing went wrong with the sync, so there is nothing to explain: the row does not open
		// for these.
		SyncStatus.Healthy,
		SyncStatus.NewStudentNoRecord -> Unit

		SyncStatus.RecordAccessDenied -> SyncStatusInfoDialog(
			titleText = stringResource(Res.string.dialog_title_record_access_denied),
			messageText = stringResource(Res.string.dialog_message_record_access_denied),
			confirmText = stringResource(Res.string.dialog_button_understood),
			onConfirmClick = {},
			onDismissRequest = onDismissRequest
		)

		SyncStatus.Unavailable -> SyncStatusInfoDialog(
			titleText = stringResource(Res.string.dialog_title_sync_unavailable),
			messageText = stringResource(Res.string.dialog_message_sync_unavailable),
			confirmText = stringResource(Res.string.dialog_button_understood),
			onConfirmClick = {},
			onDismissRequest = onDismissRequest
		)

		SyncStatus.Failed -> SyncStatusInfoDialog(
			titleText = stringResource(Res.string.dialog_title_sync_failed),
			messageText = stringResource(Res.string.dialog_message_sync_failed),
			confirmText = stringResource(Res.string.dialog_button_understood),
			onConfirmClick = {},
			onDismissRequest = onDismissRequest
		)

		SyncStatus.OutdatedCredentials,
		SyncStatus.MissingCredentials -> PasswordRequiredDialog(
			syncStatus = syncStatus,
			onUpdatePasswordClick = onUpdatePasswordClick,
			onDismissRequest = onDismissRequest
		)
	}
}

// The statuses the unavailable-sources copy must not speak for: the ones with a copy of their own
// (a password to type, a denied record) and the new student, which is not a problem at all.
private val SyncStatus.ignoresUnavailableSources: Boolean
	get() = requiresPassword ||
		this == SyncStatus.NewStudentNoRecord ||
		this == SyncStatus.RecordAccessDenied
