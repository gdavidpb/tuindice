package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.dialog_button_close
import tuindice.summary.generated.resources.dialog_button_enter_password
import tuindice.summary.generated.resources.dialog_button_update_password
import tuindice.summary.generated.resources.dialog_message_sync_missing_credentials
import tuindice.summary.generated.resources.dialog_message_sync_outdated_credentials
import tuindice.summary.generated.resources.dialog_title_sync_missing_credentials
import tuindice.summary.generated.resources.dialog_title_sync_outdated_credentials

// Both latches end the same way, with the user typing the password again.
@Composable
fun PasswordRequiredDialog(
	syncStatus: SyncStatus,
	onUpdatePasswordClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val isOutdated = syncStatus == SyncStatus.OutdatedCredentials

	SyncStatusInfoDialog(
		titleText = stringResource(
			if (isOutdated) {
				Res.string.dialog_title_sync_outdated_credentials
			} else {
				Res.string.dialog_title_sync_missing_credentials
			}
		),
		messageText = stringResource(
			if (isOutdated) {
				Res.string.dialog_message_sync_outdated_credentials
			} else {
				Res.string.dialog_message_sync_missing_credentials
			}
		),
		confirmText = stringResource(
			if (isOutdated) Res.string.dialog_button_update_password else Res.string.dialog_button_enter_password
		),
		dismissText = stringResource(Res.string.dialog_button_close),
		onConfirmClick = onUpdatePasswordClick,
		onDismissRequest = onDismissRequest
	)
}
