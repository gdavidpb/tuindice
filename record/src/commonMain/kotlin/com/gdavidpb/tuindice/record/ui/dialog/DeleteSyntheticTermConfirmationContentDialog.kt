package com.gdavidpb.tuindice.record.ui.dialog

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.dialog_button_cancel
import tuindice.record.generated.resources.dialog_button_delete
import tuindice.record.generated.resources.dialog_message_delete_synthetic_term
import tuindice.record.generated.resources.dialog_title_delete_synthetic_term

@Composable
fun DeleteSyntheticTermConfirmationContentDialog(
	onConfirmClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	DeleteSyntheticTermConfirmationDialog(
		titleText = stringResource(Res.string.dialog_title_delete_synthetic_term),
		messageText = stringResource(Res.string.dialog_message_delete_synthetic_term),
		confirmText = stringResource(Res.string.dialog_button_delete),
		cancelText = stringResource(Res.string.dialog_button_cancel),
		onConfirmClick = onConfirmClick,
		onDismissRequest = onDismissRequest
	)
}
