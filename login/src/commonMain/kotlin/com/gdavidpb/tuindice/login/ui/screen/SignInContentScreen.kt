package com.gdavidpb.tuindice.login.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.ui.view.AnimatedPatternBackground
import com.gdavidpb.tuindice.login.ui.view.SignInIdleView
import com.gdavidpb.tuindice.login.ui.view.SignInLoggingInView
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tuindice.login.generated.resources.Res
import tuindice.login.generated.resources.background
import tuindice.login.generated.resources.button_sign_in
import tuindice.login.generated.resources.hint_password
import tuindice.login.generated.resources.hint_usb_id
import tuindice.login.generated.resources.ic_launcher
import tuindice.login.generated.resources.label_policies
import tuindice.login.generated.resources.link_privacy_policy
import tuindice.login.generated.resources.link_terms_and_conditions

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
				headerContent = { LoginHeader() }
			)
		},
		loggingInContent = { loggingInState ->
			SignInLoggingInView(
				state = loggingInState,
				headerContent = { LoginHeader() }
			)
		}
	)
}

@Composable
private fun LoginHeader() {
	Image(
		modifier = Modifier.padding(vertical = 32.dp),
		painter = painterResource(Res.drawable.ic_launcher),
		contentDescription = null
	)
}
