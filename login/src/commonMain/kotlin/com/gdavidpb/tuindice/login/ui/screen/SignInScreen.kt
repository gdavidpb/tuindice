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

@Composable
fun SignInScreen(
	state: SignIn.State,
	onUsbIdChange: (usbId: String) -> Unit,
	onPasswordChange: (password: String) -> Unit,
	onSignInClick: (usbId: String, password: String) -> Unit,
	onTermsAndConditionsClick: () -> Unit,
	onPrivacyPolicyClick: () -> Unit,
	backgroundContent: @Composable () -> Unit,
	idleContent: @Composable (
		state: SignIn.State,
		onUsbIdChange: (usbId: String) -> Unit,
		onPasswordChange: (password: String) -> Unit,
		onSignInClick: (usbId: String, password: String) -> Unit,
		onTermsAndConditionsClick: () -> Unit,
		onPrivacyPolicyClick: () -> Unit
	) -> Unit,
	loggingInContent: @Composable (state: SignIn.State) -> Unit
) {
	Box(modifier = Modifier.fillMaxSize()) {
		backgroundContent()

		AnimatedContent(
			targetState = state is SignIn.State.LoggingIn,
			transitionSpec = {
				val enter = slideInHorizontally { x -> x }
				val exit = slideOutHorizontally { x -> -x }

				enter togetherWith exit
			},
			label = "AnimatedBackgroundViewAnimatedContent",
		) { isLoggingIn ->
			if (isLoggingIn)
				loggingInContent(state)
			else
				idleContent(
					state,
					onUsbIdChange,
					onPasswordChange,
					onSignInClick,
					onTermsAndConditionsClick,
					onPrivacyPolicyClick
				)
		}
	}
}
