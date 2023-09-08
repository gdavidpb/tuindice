package com.gdavidpb.tuindice.login.ui.view

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import com.gdavidpb.tuindice.login.R

@Composable
fun PasswordTextField(
	modifier: Modifier = Modifier,
	password: String,
	onPasswordChange: (password: String) -> Unit,
	error: String? = null,
	keyboardActions: KeyboardActions = KeyboardActions.Default
) {
	val passwordField = remember { mutableStateOf(TextFieldValue(password)) }
	val supportingText = remember { mutableStateOf(error) }
	val passwordVisible = remember { mutableStateOf(false) }

	OutlinedTextField(
		modifier = modifier,
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
		label = { Text(text = stringResource(R.string.hint_password)) },
		visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
		trailingIcon = {
			IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
				Icon(
					imageVector = if (passwordVisible.value) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
					contentDescription = null
				)
			}
		},
		leadingIcon = {
			Icon(
				imageVector = Icons.Outlined.Lock,
				contentDescription = null
			)
		},
		keyboardOptions = KeyboardOptions(
			imeAction = ImeAction.Done,
			keyboardType = KeyboardType.Password
		),
		keyboardActions = keyboardActions,
		singleLine = true
	)
}