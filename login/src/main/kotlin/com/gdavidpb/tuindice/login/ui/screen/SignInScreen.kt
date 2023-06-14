package com.gdavidpb.tuindice.login.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.ui.view.AnimatedBackgroundView
import com.gdavidpb.tuindice.login.ui.view.SignInIdleView
import com.gdavidpb.tuindice.login.ui.view.SignInLoggingInView

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SignInScreen(
	state: SignIn.State,
	onUsbIdChanged: (usbId: String) -> Unit,
	onPasswordChanged: (password: String) -> Unit,
	onSignInClick: () -> Unit,
	onTermsAndConditionsClick: () -> Unit,
	onPrivacyPolicyClick: () -> Unit
) {
	AnimatedBackgroundView(
		background = R.drawable.background
	) {
		AnimatedContent(
			targetState = state is SignIn.State.LoggingIn,
			transitionSpec = {
				val enter = slideInHorizontally { x -> x }
				val exit = slideOutHorizontally { x -> -x }

				enter with exit
			},
		) { isLoggingIn ->
			if (isLoggingIn)
				SignInLoggingInView(
					state = state
				)
			else
				SignInIdleView(
					state = state,
					onUsbIdChanged = onUsbIdChanged,
					onPasswordChanged = onPasswordChanged,
					onSignInClick = onSignInClick,
					onTermsAndConditionsClick = onTermsAndConditionsClick,
					onPrivacyPolicyClick = onPrivacyPolicyClick
				)
		}
	}
}

