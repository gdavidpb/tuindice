package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.dialog_button_close
import tuindice.summary.generated.resources.dialog_button_understood
import tuindice.summary.generated.resources.dialog_button_update_password
import tuindice.summary.generated.resources.dialog_message_sync_failed
import tuindice.summary.generated.resources.dialog_message_sync_outdated_credentials
import tuindice.summary.generated.resources.dialog_message_sync_unavailable
import tuindice.summary.generated.resources.dialog_title_sync_failed
import tuindice.summary.generated.resources.dialog_title_sync_outdated_credentials
import tuindice.summary.generated.resources.dialog_title_sync_unavailable

@Composable
fun SyncStatusInfoContentDialog(
	syncStatus: SyncStatus,
	onUpdatePasswordClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
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
