package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.dialog_button_close
import tuindice.summary.generated.resources.dialog_button_understood
import tuindice.summary.generated.resources.dialog_button_update_password
import tuindice.summary.generated.resources.dialog_message_sync_sources_enrollment_unavailable
import tuindice.summary.generated.resources.dialog_message_sync_failed
import tuindice.summary.generated.resources.dialog_message_sync_sources_record_and_enrollment_unavailable
import tuindice.summary.generated.resources.dialog_message_sync_sources_record_unavailable
import tuindice.summary.generated.resources.dialog_message_sync_outdated_credentials
import tuindice.summary.generated.resources.dialog_message_sync_unavailable
import tuindice.summary.generated.resources.dialog_title_sync_sources_unavailable
import tuindice.summary.generated.resources.dialog_title_sync_failed
import tuindice.summary.generated.resources.dialog_title_sync_outdated_credentials
import tuindice.summary.generated.resources.dialog_title_sync_unavailable

@Composable
fun SyncStatusInfoContentDialog(
	syncStatus: SyncStatus,
	syncReport: SyncReport = SyncReport.success(),
	onUpdatePasswordClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	if (syncStatus != SyncStatus.OutdatedCredentials && syncReport.hasUnavailableSource) {
		SyncStatusInfoDialog(
			titleText = stringResource(Res.string.dialog_title_sync_sources_unavailable),
			messageText = syncReport.unavailableSourcesMessage(),
			confirmText = stringResource(Res.string.dialog_button_understood),
			onConfirmClick = {},
			onDismissRequest = onDismissRequest
		)
		return
	}

	when (syncStatus) {
		SyncStatus.Healthy -> Unit

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

		SyncStatus.OutdatedCredentials -> SyncStatusInfoDialog(
			titleText = stringResource(Res.string.dialog_title_sync_outdated_credentials),
			messageText = stringResource(Res.string.dialog_message_sync_outdated_credentials),
			confirmText = stringResource(Res.string.dialog_button_update_password),
			dismissText = stringResource(Res.string.dialog_button_close),
			onConfirmClick = onUpdatePasswordClick,
			onDismissRequest = onDismissRequest
		)
	}
}

@Composable
private fun SyncReport.unavailableSourcesMessage(): String {
	val recordUnavailable = sources.record.status == SyncSourceStatus.Unavailable
	val enrollmentUnavailable = sources.enrollment.status == SyncSourceStatus.Unavailable

	return when {
		recordUnavailable && enrollmentUnavailable ->
			stringResource(Res.string.dialog_message_sync_sources_record_and_enrollment_unavailable)

		recordUnavailable ->
			stringResource(Res.string.dialog_message_sync_sources_record_unavailable)

		enrollmentUnavailable ->
			stringResource(Res.string.dialog_message_sync_sources_enrollment_unavailable)

		else ->
			stringResource(Res.string.dialog_message_sync_unavailable)
	}
}
