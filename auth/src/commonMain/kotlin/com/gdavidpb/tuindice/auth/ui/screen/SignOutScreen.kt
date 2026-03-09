package com.gdavidpb.tuindice.auth.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.auth.ui.dialog.SignOutDialog
import org.jetbrains.compose.resources.stringResource
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.dialog_button_cancel
import tuindice.auth.generated.resources.dialog_button_sign_out
import tuindice.auth.generated.resources.dialog_message_sign_out
import tuindice.auth.generated.resources.dialog_title_sign_out

@Composable
fun SignOutScreen(
	state: SignOut.State,
	onConfirmClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	SignOutDialog(
		state = state,
		titleText = stringResource(Res.string.dialog_title_sign_out),
		messageText = stringResource(Res.string.dialog_message_sign_out),
		confirmText = stringResource(Res.string.dialog_button_sign_out),
		cancelText = stringResource(Res.string.dialog_button_cancel),
		onConfirmClick = onConfirmClick,
		onDismissRequest = onDismissRequest
	)
}
