package com.gdavidpb.tuindice.login.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.gdavidpb.tuindice.base.ui.anim.animateShake
import com.gdavidpb.tuindice.base.ui.anim.rememberAnimatableShake
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.ui.view.AnimatedBackgroundView
import com.gdavidpb.tuindice.login.ui.view.FlipperView
import com.gdavidpb.tuindice.login.ui.view.LinkText
import com.gdavidpb.tuindice.login.ui.view.PasswordTextField
import com.gdavidpb.tuindice.login.ui.view.UsbIdTextField
import com.gdavidpb.tuindice.login.utils.extension.isUsbId
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
	state: SignIn.State,
	onUsbIdChanged: (usbId: String) -> Unit,
	onPasswordChanged: (password: String) -> Unit,
	onSignInClick: () -> Unit,
	onTermsAndConditionsClick: () -> Unit,
	onPrivacyPolicyClick: () -> Unit
) {
	val coroutineScope = rememberCoroutineScope()
	val logoAnimation = rememberAnimatableShake()

	AnimatedBackgroundView(
		background = R.drawable.background
	) {
		Column(
			modifier = Modifier.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			Image(
				modifier = Modifier
					.padding(
						vertical = dimensionResource(id = R.dimen.dp_32)
					)
					.clickable {
						coroutineScope.launch { logoAnimation.animateShake() }
					}
					.rotate(logoAnimation.value),
				painter = painterResource(id = R.drawable.ic_launcher),
				contentDescription = null
			)

			when (state) {
				is SignIn.State.Idle -> {
					val terms = stringResource(id = R.string.link_terms_and_conditions)
					val privacy = stringResource(id = R.string.link_privacy_policy)
					val isSignInEnabled =
						state.usbId.isUsbId() && state.password.isNotBlank()

					val links = mapOf(
						terms to onTermsAndConditionsClick,
						privacy to onPrivacyPolicyClick
					)

					UsbIdTextField(
						modifier = Modifier,
						usbId = state.usbId,
						onValueChange = onUsbIdChanged
					)

					PasswordTextField(
						password = state.password,
						onValueChange = onPasswordChanged,
						keyboardActions = KeyboardActions(onDone = {
							if (isSignInEnabled) onSignInClick()
						})
					)

					Button(
						modifier = Modifier
							.fillMaxWidth()
							.padding(
								vertical = dimensionResource(id = R.dimen.dp_32),
								horizontal = dimensionResource(id = R.dimen.dp_32)
							),
						enabled = isSignInEnabled,
						onClick = onSignInClick
					) {
						Text(text = stringResource(id = R.string.button_sign_in))
					}

					LinkText(
						text = stringResource(id = R.string.label_policies),
						style = TextStyle(
							textAlign = TextAlign.Center,
							color = MaterialTheme.colorScheme.onBackground
						) + MaterialTheme.typography.bodyMedium,
						linkStyle = SpanStyle(
							color = MaterialTheme.colorScheme.surfaceTint,
							textDecoration = TextDecoration.Underline
						),
						links = links
					)
				}

				is SignIn.State.LoggingIn -> {
					FlipperView(items = state.messages)
				}

				is SignIn.State.LoggedIn -> {
				}
			}
		}
	}
}