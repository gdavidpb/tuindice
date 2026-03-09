package com.gdavidpb.tuindice.login.ui.view

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import com.gdavidpb.tuindice.login.ui.LoginUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.login.generated.resources.Res
import tuindice.login.generated.resources.a11y_hide_password
import tuindice.login.generated.resources.a11y_show_password

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
	val passwordField = remember { mutableStateOf(TextFieldValue(password)) }
	val supportingText = remember { mutableStateOf(error) }

	OutlinedTextField(
		modifier = modifier.testTag(LoginUiTags.PasswordTextField),
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
				modifier = Modifier.testTag(LoginUiTags.PasswordToggle),
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
