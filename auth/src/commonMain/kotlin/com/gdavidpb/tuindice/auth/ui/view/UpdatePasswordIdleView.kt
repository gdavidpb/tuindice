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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.auth.presentation.contract.UpdatePassword

@Composable
fun UpdatePasswordIdleView(
	state: UpdatePassword.State.Idle,
	onPasswordChange: (password: String) -> Unit,
	onPasswordVisibilityToggle: () -> Unit,
	onConfirmClick: (password: String) -> Unit,
	appNameText: String,
	messageText: String,
	passwordLabelText: String
) {
	val annotatedString = remember {
		buildAnnotatedString {
			val start = messageText.indexOf(appNameText)
			val end = start + appNameText.length

			append(messageText)

			addStyle(
				style = SpanStyle(fontWeight = FontWeight.Bold),
				start = start,
				end = end
			)
		}
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
			onPasswordChange = onPasswordChange,
			onPasswordVisibilityToggle = onPasswordVisibilityToggle,
			error = state.error,
			imeAction = ImeAction.Done,
			keyboardActions = KeyboardActions(onDone = {
				if (state.password.isNotEmpty())
					onConfirmClick(state.password)
			})
		)
	}
}
