package com.gdavidpb.tuindice.login.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.login.ui.dialog.UpdatePasswordDialog
import org.jetbrains.compose.resources.stringResource
import tuindice.login.generated.resources.Res
import tuindice.login.generated.resources.app_name
import tuindice.login.generated.resources.dialog_button_update_password_confirm
import tuindice.login.generated.resources.dialog_button_update_password_later
import tuindice.login.generated.resources.dialog_message_update_password
import tuindice.login.generated.resources.dialog_title_update_password
import tuindice.login.generated.resources.dialog_title_updating_password
import tuindice.login.generated.resources.hint_password

@Composable
fun UpdatePasswordScreen(
	state: UpdatePassword.State,
	onPasswordChange: (String) -> Unit,
	onConfirmClick: (password: String) -> Unit,
	onDismissRequest: () -> Unit
) {
	UpdatePasswordDialog(
		state = state,
		titleText = stringResource(Res.string.dialog_title_update_password),
		updatingTitleText = stringResource(Res.string.dialog_title_updating_password),
		confirmText = stringResource(Res.string.dialog_button_update_password_confirm),
		laterText = stringResource(Res.string.dialog_button_update_password_later),
		appNameText = stringResource(Res.string.app_name),
		messageText = stringResource(Res.string.dialog_message_update_password),
		passwordLabelText = stringResource(Res.string.hint_password),
		onPasswordChange = onPasswordChange,
		onConfirmClick = onConfirmClick,
		onDismissRequest = onDismissRequest
	)
}
