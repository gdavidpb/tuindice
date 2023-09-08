package com.gdavidpb.tuindice.login.ui.view

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.utils.extension.isUsbId

@Composable
fun SignInIdleView(
	state: SignIn.State,
	onUsbIdChange: (usbId: String) -> Unit,
	onPasswordChange: (password: String) -> Unit,
	onSignInClick: () -> Unit,
	onTermsAndConditionsClick: () -> Unit,
	onPrivacyPolicyClick: () -> Unit
) {
	if (state !is SignIn.State.Idle) return

	val terms = stringResource(id = R.string.link_terms_and_conditions)
	val privacy = stringResource(id = R.string.link_privacy_policy)
	val isSignInEnabled = state.usbId.isUsbId() && state.password.isNotEmpty()

	val links = mapOf(
		terms to onTermsAndConditionsClick,
		privacy to onPrivacyPolicyClick
	)

	Column(
		modifier = Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Image(
			modifier = Modifier
				.padding(
					vertical = dimensionResource(id = R.dimen.dp_32)
				),
			painter = painterResource(id = R.drawable.ic_launcher),
			contentDescription = null
		)

		UsbIdTextField(
			modifier = Modifier
				.fillMaxWidth()
				.padding(
					vertical = dimensionResource(id = R.dimen.dp_8),
					horizontal = dimensionResource(id = R.dimen.dp_32)
				),
			usbId = state.usbId,
			onUsbIdChange = onUsbIdChange
		)

		PasswordTextField(
			modifier = Modifier
				.fillMaxWidth()
				.padding(
					horizontal = dimensionResource(id = R.dimen.dp_32)
				),
			password = state.password,
			onPasswordChange = onPasswordChange,
			keyboardActions = KeyboardActions(onDone = {
				if (isSignInEnabled) onSignInClick()
			})
		)

		Button(
			modifier = Modifier
				.fillMaxWidth()
				.padding(
					vertical = dimensionResource(id = R.dimen.dp_16),
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
}
