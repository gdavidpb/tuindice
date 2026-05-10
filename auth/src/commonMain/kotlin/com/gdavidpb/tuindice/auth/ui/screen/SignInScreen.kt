package com.gdavidpb.tuindice.auth.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.ui.view.AnimatedPatternBackground
import com.gdavidpb.tuindice.auth.ui.view.SignInIdleView
import com.gdavidpb.tuindice.auth.ui.view.SignInLoggingInView
import org.jetbrains.compose.resources.stringResource
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.background
import tuindice.auth.generated.resources.button_sign_in
import tuindice.auth.generated.resources.hint_password
import tuindice.auth.generated.resources.hint_usb_id
import tuindice.auth.generated.resources.label_policies
import tuindice.auth.generated.resources.link_privacy_policy
import tuindice.auth.generated.resources.link_terms_and_conditions

@Composable
fun SignInScreen(
	state: SignIn.State,
	onUsbIdChange: (usbId: String) -> Unit,
	onPasswordChange: (password: String) -> Unit,
	onPasswordVisibilityToggle: () -> Unit,
	onSignInClick: (usbId: String, password: String) -> Unit,
	onTermsAndConditionsClick: () -> Unit,
	onPrivacyPolicyClick: () -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		AnimatedPatternBackground(background = Res.drawable.background)

		AnimatedContent(
			targetState = state,
			contentKey = { targetState -> targetState::class },
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
						onPasswordVisibilityToggle = onPasswordVisibilityToggle,
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
