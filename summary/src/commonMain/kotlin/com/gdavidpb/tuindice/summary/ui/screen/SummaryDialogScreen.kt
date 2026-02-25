package com.gdavidpb.tuindice.summary.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.summary.ui.dialog.ProfilePictureSettingsDialog
import com.gdavidpb.tuindice.summary.ui.dialog.RemoveProfilePictureConfirmationDialog
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.dialog_button_cancel
import tuindice.summary.generated.resources.dialog_button_remove
import tuindice.summary.generated.resources.dialog_message_remove_profile_picture
import tuindice.summary.generated.resources.dialog_title_remove_profile_picture
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
