package com.gdavidpb.tuindice.login.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword

@Composable
fun UpdatePasswordIdleView(
	state: UpdatePassword.State,
	onPasswordChange: (password: String) -> Unit,
	onConfirmClick: () -> Unit,
) {
	if (state !is UpdatePassword.State.Idle) return

	val appName = stringResource(id = R.string.app_name)
	val message = stringResource(R.string.dialog_message_update_password)

	val annotatedString = remember {
		buildAnnotatedString {
			val start = message.indexOf(appName)
			val end = start + appName.length

			append(message)

			addStyle(
				style = SpanStyle(fontWeight = FontWeight.Bold),
				start = start,
				end = end
			)
		}
	}

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Text(
			text = annotatedString,
			style = MaterialTheme.typography.bodyLarge
		)

		PasswordTextField(
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = dimensionResource(id = R.dimen.dp_16)),
			password = state.password,
			onPasswordChange = onPasswordChange,
			error = state.error,
			keyboardActions = KeyboardActions(onDone = {
				if (state.password.isNotEmpty()) onConfirmClick()
			})
		)
	}
}