package com.gdavidpb.tuindice.login.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.presentation.contract.SignOut
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.login.ui.dialog.SignOutDialog
import com.gdavidpb.tuindice.login.ui.dialog.UpdatePasswordDialog
import com.gdavidpb.tuindice.login.ui.view.AnimatedPatternBackground
import com.gdavidpb.tuindice.login.ui.view.SignInIdleView
import com.gdavidpb.tuindice.login.ui.view.SignInLoggingInView
import com.gdavidpb.tuindice.login.ui.view.UpdatePasswordIdleView
import com.gdavidpb.tuindice.login.ui.view.UpdatePasswordLoggingInView
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tuindice.login.generated.resources.*

@Composable
fun SignInContentScreen(
	state: SignIn.State,
	onUsbIdChange: (String) -> Unit,
	onPasswordChange: (String) -> Unit,
	onSignInClick: (usbId: String, password: String) -> Unit,
	onTermsAndConditionsClick: () -> Unit,
	onPrivacyPolicyClick: () -> Unit
) {
	SignInScreen(
		state = state,
		onUsbIdChange = onUsbIdChange,
		onPasswordChange = onPasswordChange,
		onSignInClick = onSignInClick,
		onTermsAndConditionsClick = onTermsAndConditionsClick,
		onPrivacyPolicyClick = onPrivacyPolicyClick,
		backgroundContent = { AnimatedPatternBackground(background = Res.drawable.background) },
		idleContent = { idleState, usbIdChange, passwordChange, signInClick, termsClick, privacyClick ->
			SignInIdleView(
				state = idleState,
				onUsbIdChange = usbIdChange,
				onPasswordChange = passwordChange,
				onSignInClick = signInClick,
				onTermsAndConditionsClick = termsClick,
				onPrivacyPolicyClick = privacyClick,
				termsAndConditionsText = stringResource(Res.string.link_terms_and_conditions),
				privacyPolicyText = stringResource(Res.string.link_privacy_policy),
				policiesText = stringResource(Res.string.label_policies),
				usbIdLabelText = stringResource(Res.string.hint_usb_id),
				passwordLabelText = stringResource(Res.string.hint_password),
				signInButtonText = stringResource(Res.string.button_sign_in),
				headerContent = { IosLoginHeader() }
			)
		},
		loggingInContent = { loggingInState ->
			SignInLoggingInView(
				state = loggingInState,
				headerContent = { IosLoginHeader() }
			)
		}
	)
}

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

@Composable
fun UpdatePasswordContentDialog(
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
		onPasswordChange = onPasswordChange,
		onConfirmClick = onConfirmClick,
		onDismissRequest = onDismissRequest,
		idleContent = { idleState, passwordChange, confirmClick ->
			UpdatePasswordIdleView(
				state = idleState,
				onPasswordChange = passwordChange,
				onConfirmClick = confirmClick,
				appNameText = stringResource(Res.string.app_name),
				messageText = stringResource(Res.string.dialog_message_update_password),
				passwordLabelText = stringResource(Res.string.hint_password)
			)
		},
		updatingContent = {
			UpdatePasswordLoggingInView()
		}
	)
}

@Composable
private fun IosLoginHeader() {
	Image(
		modifier = Modifier.padding(vertical = 32.dp),
		painter = painterResource(Res.drawable.ic_launcher),
		contentDescription = null
	)
}
