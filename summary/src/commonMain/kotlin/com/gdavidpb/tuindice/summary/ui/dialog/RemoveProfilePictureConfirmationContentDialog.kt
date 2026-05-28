package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.dialog_button_cancel
import tuindice.summary.generated.resources.dialog_button_remove
import tuindice.summary.generated.resources.dialog_message_remove_profile_picture
import tuindice.summary.generated.resources.dialog_title_remove_profile_picture

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
