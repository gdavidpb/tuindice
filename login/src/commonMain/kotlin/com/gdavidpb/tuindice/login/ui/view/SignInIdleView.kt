package com.gdavidpb.tuindice.login.ui.view

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.utils.extension.isUsbId

@Composable
fun SignInIdleView(
	state: SignIn.State.Idle,
	onUsbIdChange: (usbId: String) -> Unit,
	onPasswordChange: (password: String) -> Unit,
	onSignInClick: (usbId: String, password: String) -> Unit,
	onTermsAndConditionsClick: () -> Unit,
	onPrivacyPolicyClick: () -> Unit,
	termsAndConditionsText: String,
	privacyPolicyText: String,
	policiesText: String,
	usbIdLabelText: String,
	passwordLabelText: String,
	signInButtonText: String,
	headerContent: @Composable () -> Unit = {}
) {
	val isSignInEnabled = state.usbId.isUsbId() && state.password.isNotEmpty()

	val links = mapOf(
		termsAndConditionsText to onTermsAndConditionsClick,
		privacyPolicyText to onPrivacyPolicyClick
	)

	Column(
		modifier = Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		headerContent()

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
			onPasswordChange = onPasswordChange,
			imeAction = ImeAction.Done,
			keyboardActions = KeyboardActions(onDone = {
				if (isSignInEnabled) onSignInClick(state.usbId, state.password)
			})
		)

		Button(
			modifier = Modifier
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

		LinkText(
			text = policiesText,
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
}
