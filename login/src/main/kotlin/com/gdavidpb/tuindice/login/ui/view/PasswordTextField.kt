package com.gdavidpb.tuindice.login.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import com.gdavidpb.tuindice.login.R

@Composable
fun PasswordTextField(
	password: String,
	error: String? = null,
	onValueChange: (password: String) -> Unit,
	keyboardActions: KeyboardActions = KeyboardActions.Default
) {
	val textField = remember { mutableStateOf(TextFieldValue(password)) }
	val supportingText = remember { mutableStateOf(error) }

	OutlinedTextField(
		modifier = Modifier
			.fillMaxWidth()
			.padding(
				horizontal = dimensionResource(id = R.dimen.dp_32)
			),
		value = textField.value,
		onValueChange = { newValue ->
			supportingText.value = null

			textField.value = newValue

			onValueChange(textField.value.text)
		},
		isError = supportingText.value != null,
		supportingText = {
			val text = supportingText.value

			if (text != null) Text(text)
		},
		label = { Text(text = stringResource(R.string.hint_password)) },
		visualTransformation = PasswordVisualTransformation(),
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