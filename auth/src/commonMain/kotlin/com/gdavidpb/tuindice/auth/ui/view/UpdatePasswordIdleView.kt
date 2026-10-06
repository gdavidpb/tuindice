package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.auth.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.auth.ui.AuthUiTags

@Composable
fun UpdatePasswordIdleView(
	state: UpdatePassword.State.Idle,
	onPasswordChange: (password: String) -> Unit,
	onPasswordVisibilityToggle: () -> Unit,
	onConfirmClick: () -> Unit,
	appNameText: String,
	messageText: String,
	passwordLabelText: String,
	enabled: Boolean = true
) {
	val annotatedString = remember(messageText, appNameText) {
		messageText.withBoldName(name = appNameText)
	}

	Column(
		modifier = Modifier
			.testTag(AuthUiTags.UpdatePasswordIdleContainer)
			.fillMaxWidth()
	) {
		Text(
			text = annotatedString,
			style = MaterialTheme.typography.bodyLarge
		)

		PasswordTextField(
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 16.dp),
			labelText = passwordLabelText,
			password = state.password,
			isPasswordVisible = state.isPasswordVisible,
			enabled = enabled,
			onPasswordChange = onPasswordChange,
			onPasswordVisibilityToggle = onPasswordVisibilityToggle,
			error = state.error,
			imeAction = ImeAction.Done,
			keyboardActions = KeyboardActions(onDone = {
				if (enabled && state.password.isNotEmpty())
					onConfirmClick()
			})
		)
	}
}

// The message is a string resource: when its wording stops naming the app there is nothing to
// bold, and a span from -1 crashes the text layout on Android.
internal fun String.withBoldName(name: String): AnnotatedString {
	val message = this

	return buildAnnotatedString {
		append(message)

		val start = message.indexOf(name)

		if (name.isNotEmpty() && start >= 0) {
			addStyle(
				style = SpanStyle(fontWeight = FontWeight.Bold),
				start = start,
				end = start + name.length
			)
		}
	}
}
