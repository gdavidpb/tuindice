package com.gdavidpb.tuindice.auth.ui.view

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.utils.extension.isUsbId
import com.gdavidpb.tuindice.base.ui.view.AppLogoView

@Composable
fun SignInIdleView(
	state: SignIn.State.Idle,
	onUsbIdChange: (usbId: String) -> Unit,
	onPasswordChange: (password: String) -> Unit,
	onPasswordVisibilityToggle: () -> Unit,
	onSignInClick: (usbId: String, password: String) -> Unit,
	onTermsAndConditionsClick: () -> Unit,
	onPrivacyPolicyClick: () -> Unit,
	termsAndConditionsText: String,
	privacyPolicyText: String,
	policiesText: String,
	usbIdLabelText: String,
	passwordLabelText: String,
	signInButtonText: String
) {
	val isSignInEnabled = state.usbId.isUsbId() && state.password.isNotEmpty()
	val policyIntroText = policiesText.substringBefore(termsAndConditionsText).trimEnd()
	val policyTextStyle = TextStyle(
		textAlign = TextAlign.Center,
		color = MaterialTheme.colorScheme.onBackground
	) + MaterialTheme.typography.bodyMedium
	val policyLinkStyle = policyTextStyle + SpanStyle(
		color = MaterialTheme.colorScheme.surfaceTint,
		textDecoration = TextDecoration.Underline
	)

	Column(
		modifier = Modifier
			.testTag(AuthUiTags.SignInIdleContainer)
			.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		AppLogoView(
			modifier = Modifier.padding(vertical = 32.dp),
			contentDescription = null
		)

		UsbIdTextField(
			modifier = Modifier
				.fillMaxWidth()
				.padding(
					vertical = 8.dp,
					horizontal = 32.dp
				),
			labelText = usbIdLabelText,
			usbId = state.usbId,
			onUsbIdChange = onUsbIdChange
		)

		PasswordTextField(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 32.dp),
			labelText = passwordLabelText,
			password = state.password,
			isPasswordVisible = state.isPasswordVisible,
			onPasswordChange = onPasswordChange,
			onPasswordVisibilityToggle = onPasswordVisibilityToggle,
			imeAction = ImeAction.Done,
			keyboardActions = KeyboardActions(onDone = {
				if (isSignInEnabled) onSignInClick(state.usbId, state.password)
			})
		)

		Button(
			modifier = Modifier
				.testTag(AuthUiTags.SignInButton)
				.fillMaxWidth()
				.padding(
					vertical = 16.dp,
					horizontal = 32.dp
				),
			enabled = isSignInEnabled,
			onClick = { onSignInClick(state.usbId, state.password) }
		) {
			Text(text = signInButtonText)
		}

		Column(
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Text(
				text = policyIntroText,
				style = policyTextStyle
			)

			Text(
				modifier = Modifier
					.testTag(AuthUiTags.TermsAndConditionsLink)
					.clickable(onClick = onTermsAndConditionsClick),
				text = termsAndConditionsText,
				style = policyLinkStyle
			)

			Text(
				text = "y",
				style = policyTextStyle
			)

			Text(
				modifier = Modifier
					.testTag(AuthUiTags.PrivacyPolicyLink)
					.clickable(onClick = onPrivacyPolicyClick),
				text = privacyPolicyText,
				style = policyLinkStyle
			)
		}
	}
}
