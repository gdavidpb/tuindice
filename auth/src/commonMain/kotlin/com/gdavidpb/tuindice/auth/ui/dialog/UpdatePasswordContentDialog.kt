package com.gdavidpb.tuindice.auth.ui.dialog

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.auth.presentation.contract.UpdatePassword
import org.jetbrains.compose.resources.stringResource
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.app_name
import tuindice.auth.generated.resources.dialog_button_update_password_confirm
import tuindice.auth.generated.resources.dialog_button_update_password_later
import tuindice.auth.generated.resources.dialog_message_update_password
import tuindice.auth.generated.resources.dialog_title_update_password
import tuindice.auth.generated.resources.dialog_title_updating_password
import tuindice.auth.generated.resources.hint_password

@Composable
fun UpdatePasswordContentDialog(
	state: UpdatePassword.State,
	onPasswordChange: (String) -> Unit,
	onPasswordVisibilityToggle: () -> Unit,
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
		onPasswordVisibilityToggle = onPasswordVisibilityToggle,
		onConfirmClick = onConfirmClick,
		onDismissRequest = onDismissRequest
	)
}
