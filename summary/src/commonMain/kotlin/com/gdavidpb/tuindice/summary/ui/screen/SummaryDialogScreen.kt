package com.gdavidpb.tuindice.summary.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.summary.ui.dialog.ProfilePictureSettingsDialog
import com.gdavidpb.tuindice.summary.ui.dialog.RemoveProfilePictureConfirmationDialog
import com.gdavidpb.tuindice.summary.ui.dialog.SyncStatusInfoDialog
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.dialog_button_cancel
import tuindice.summary.generated.resources.dialog_button_close
import tuindice.summary.generated.resources.dialog_button_remove
import tuindice.summary.generated.resources.dialog_button_update_password
import tuindice.summary.generated.resources.dialog_message_remove_profile_picture
import tuindice.summary.generated.resources.dialog_message_sync_outdated_credentials
import tuindice.summary.generated.resources.dialog_title_remove_profile_picture
import tuindice.summary.generated.resources.dialog_title_sync_outdated_credentials
import tuindice.summary.generated.resources.menu_pick_profile_picture
import tuindice.summary.generated.resources.menu_remove_profile_picture
import tuindice.summary.generated.resources.menu_take_profile_picture
import tuindice.summary.generated.resources.title_menu_profile_picture

@Composable
fun RemoveProfilePictureConfirmationContentDialog(
	onConfirmClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	RemoveProfilePictureConfirmationDialog(
		titleText = stringResource(Res.string.dialog_title_remove_profile_picture),
		messageText = stringResource(Res.string.dialog_message_remove_profile_picture),
		confirmText = stringResource(Res.string.dialog_button_remove),
		cancelText = stringResource(Res.string.dialog_button_cancel),
		onConfirmClick = onConfirmClick,
		onDismissRequest = onDismissRequest
	)
}

@Composable
fun SyncStatusInfoContentDialog(
	syncStatus: SyncStatus,
	onUpdatePasswordClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	when (syncStatus) {
		SyncStatus.Healthy -> Unit

		SyncStatus.Unavailable,
		SyncStatus.Failed,
		-> Unit

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
fun ProfilePictureSettingsContentDialog(
	showRemove: Boolean,
	isCameraAvailable: Boolean,
	onPickPictureClick: () -> Unit,
	onTakePictureClick: () -> Unit,
	onRemovePictureClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	ProfilePictureSettingsDialog(
		showRemove = showRemove,
		isCameraAvailable = isCameraAvailable,
		titleText = stringResource(Res.string.title_menu_profile_picture),
		pickPictureLabel = stringResource(Res.string.menu_pick_profile_picture),
		takePictureLabel = stringResource(Res.string.menu_take_profile_picture),
		removePictureLabel = stringResource(Res.string.menu_remove_profile_picture),
		onPickPictureClick = onPickPictureClick,
		onTakePictureClick = onTakePictureClick,
		onRemovePictureClick = onRemovePictureClick,
		onDismissRequest = onDismissRequest
	)
}
