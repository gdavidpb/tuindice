package com.gdavidpb.tuindice.auth.ui.view

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.ui.text.EditableTextFieldState
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
	enabled: Boolean = true,
	onPasswordChange: (password: String) -> Unit,
	onPasswordVisibilityToggle: () -> Unit = {},
	error: String? = null,
	imeAction: ImeAction = ImeAction.Default,
	keyboardActions: KeyboardActions = KeyboardActions.Default
) {
	val field = remember { EditableTextFieldState(password) }
	val supportingText = remember { mutableStateOf(error) }

	field.syncExternal(password, resetKey = null)

	LaunchedEffect(error) {
		supportingText.value = error
	}

	OutlinedTextField(
		modifier = modifier.testTag(AuthUiTags.PasswordTextField),
		value = field.value,
		enabled = enabled,
		onValueChange = { newValue ->
			supportingText.value = null

			if (field.edit(newValue)) onPasswordChange(newValue.text)
		},
		isError = supportingText.value != null,
		supportingText = {
			val text = supportingText.value

			if (text != null) Text(text)
		},
		label = { Text(text = labelText) },
		leadingIcon = {
			Icon(
				imageVector = Icons.Outlined.Lock,
				contentDescription = null
			)
		},
		trailingIcon = {
			IconButton(
				modifier = Modifier.testTag(AuthUiTags.PasswordToggle),
				onClick = onPasswordVisibilityToggle,
				enabled = enabled
			) {
				Icon(
					imageVector = if (isPasswordVisible)
						Icons.Outlined.VisibilityOff
					else
						Icons.Outlined.Visibility,
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
			autoCorrectEnabled = false,
			imeAction = imeAction,
			keyboardType = KeyboardType.Password
		),
		keyboardActions = keyboardActions,
		singleLine = true
	)
}
