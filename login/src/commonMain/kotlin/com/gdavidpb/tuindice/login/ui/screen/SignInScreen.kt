package com.gdavidpb.tuindice.login.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.ui.view.AnimatedPatternBackground
import com.gdavidpb.tuindice.login.ui.view.SignInIdleView
import com.gdavidpb.tuindice.login.ui.view.SignInLoggingInView
import org.jetbrains.compose.resources.stringResource
import tuindice.login.generated.resources.Res
import tuindice.login.generated.resources.background
import tuindice.login.generated.resources.button_sign_in
import tuindice.login.generated.resources.hint_password
import tuindice.login.generated.resources.hint_usb_id
import tuindice.login.generated.resources.label_policies
import tuindice.login.generated.resources.link_privacy_policy
import tuindice.login.generated.resources.link_terms_and_conditions

@Composable
fun SignInScreen(
	state: SignIn.State,
	onUsbIdChange: (usbId: String) -> Unit,
	onPasswordChange: (password: String) -> Unit,
	onSignInClick: (usbId: String, password: String) -> Unit,
	onTermsAndConditionsClick: () -> Unit,
	onPrivacyPolicyClick: () -> Unit
) {
	Box(modifier = Modifier.fillMaxSize()) {
		AnimatedPatternBackground(background = Res.drawable.background)

		AnimatedContent(
			targetState = state,
			transitionSpec = {
				val enter = slideInHorizontally { x -> x }
				val exit = slideOutHorizontally { x -> -x }

				enter togetherWith exit
			},
			label = "AnimatedBackgroundViewAnimatedContent",
		) { targetState ->
			when (targetState) {
				is SignIn.State.Idle ->
					SignInIdleView(
						state = targetState,
						onUsbIdChange = onUsbIdChange,
						onPasswordChange = onPasswordChange,
						onSignInClick = onSignInClick,
						onTermsAndConditionsClick = onTermsAndConditionsClick,
						onPrivacyPolicyClick = onPrivacyPolicyClick,
						termsAndConditionsText = stringResource(Res.string.link_terms_and_conditions),
						privacyPolicyText = stringResource(Res.string.link_privacy_policy),
						policiesText = stringResource(Res.string.label_policies),
						usbIdLabelText = stringResource(Res.string.hint_usb_id),
						passwordLabelText = stringResource(Res.string.hint_password),
						signInButtonText = stringResource(Res.string.button_sign_in)
					)

				is SignIn.State.LoggingIn ->
					SignInLoggingInView(
						state = targetState
					)
			}
		}
	}
}
