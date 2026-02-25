package com.gdavidpb.tuindice.login.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.login.presentation.contract.SignOut
import com.gdavidpb.tuindice.login.ui.dialog.SignOutDialog
import org.jetbrains.compose.resources.stringResource
import tuindice.login.generated.resources.Res
import tuindice.login.generated.resources.dialog_button_cancel
import tuindice.login.generated.resources.dialog_button_sign_out
import tuindice.login.generated.resources.dialog_message_sign_out
import tuindice.login.generated.resources.dialog_title_sign_out

@Composable
fun SignOutContentDialog(
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
