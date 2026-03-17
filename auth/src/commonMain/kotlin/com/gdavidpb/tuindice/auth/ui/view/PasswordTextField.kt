package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.a11y_hide_password
import tuindice.auth.generated.resources.a11y_show_password

@Composable
fun PasswordTextField(
	modifier: Modifier = Modifier,
	labelText: String,
	password: String,
	isPasswordVisible: Boolean = false,
	onPasswordChange: (password: String) -> Unit,
	onPasswordVisibilityToggle: () -> Unit = {},
	error: String? = null,
	imeAction: ImeAction = ImeAction.Default,
	keyboardActions: KeyboardActions = KeyboardActions.Default
) {
	val passwordField = remember {
		mutableStateOf(
			TextFieldValue(
				text = password,
				selection = TextRange(password.length)
			)
		)
	}
	val supportingText = remember { mutableStateOf(error) }

	LaunchedEffect(password) {
		if (passwordField.value.text != password) {
			passwordField.value = TextFieldValue(
				text = password,
				selection = TextRange(password.length)
			)
		}
	}

	LaunchedEffect(error) {
		supportingText.value = error
	}

	OutlinedTextField(
		modifier = modifier.testTag(AuthUiTags.PasswordTextField),
		value = passwordField.value,
		onValueChange = { newValue ->
			supportingText.value = null

			passwordField.value = newValue

			onPasswordChange(passwordField.value.text)
		},
		isError = supportingText.value != null,
			supportingText = {
				val text = supportingText.value

				if (text != null) Text(text)
			},
		label = { Text(text = labelText) },
		leadingIcon = {
			Icon(
				imageVector = Icons.Filled.Lock,
				contentDescription = null
			)
		},
		trailingIcon = {
			IconButton(
				modifier = Modifier.testTag(AuthUiTags.PasswordToggle),
				onClick = onPasswordVisibilityToggle
			) {
				Icon(
					imageVector = if (isPasswordVisible)
						Icons.Filled.VisibilityOff
					else
						Icons.Filled.Visibility,
					contentDescription = if (isPasswordVisible)
						stringResource(Res.string.a11y_hide_password)
					else
						stringResource(Res.string.a11y_show_password)
				)
			}
		},
		visualTransformation = if (isPasswordVisible) {
			VisualTransformation.None
		} else {
			PasswordVisualTransformation()
		},
		keyboardOptions = KeyboardOptions(
			imeAction = imeAction,
			keyboardType = KeyboardType.Password
		),
		keyboardActions = keyboardActions,
		singleLine = true
	)
}
