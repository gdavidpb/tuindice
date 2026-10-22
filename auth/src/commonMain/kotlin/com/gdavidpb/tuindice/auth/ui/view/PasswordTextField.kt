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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.ui.text.EditableTextFieldState
import org.jetbrains.compose.resources.stringResource
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.a11y_hide_password
import tuindice.auth.generated.resources.a11y_show_password

/**
 * [isWaiting] is true while the owner of the state is busy (signing in, updating the password) and the
 * view model drops the edits it receives. The field takes no edits then. A key typed just before the
 * wait reaches the screen is shown but dropped, so the field readopts the state's text when the wait
 * starts and when it ends. It changes nothing when no key was dropped.
 */
@Composable
fun PasswordTextField(
	modifier: Modifier = Modifier,
	labelText: String,
	password: String,
	isPasswordVisible: Boolean = false,
	enabled: Boolean = true,
	isWaiting: Boolean,
	onPasswordChange: (password: String) -> Unit,
	onPasswordVisibilityToggle: () -> Unit = {},
	error: String? = null,
	errorModifier: Modifier = Modifier,
	isError: Boolean = false,
	imeAction: ImeAction = ImeAction.Default,
	keyboardActions: KeyboardActions = KeyboardActions.Default
) {
	val field = remember { EditableTextFieldState(password, isWaiting) }

	field.syncExternal(password, isWaiting)

	OutlinedTextField(
		modifier = modifier
			.testTag(AuthUiTags.PasswordTextField)
			.semantics {
				contentType = ContentType.Password

				// The reason for the rejection, instead of the default text of Material ("Invalid input").
				if (error != null) error(error)
			},
		value = field.value,
		enabled = enabled,
		onValueChange = { newValue ->
			// While the owner is busy the view model drops what it is told: a key taken now would be shown
			// and lost, or kept by the field and unknown to the view model.
			if (!isWaiting) {
				if (field.edit(newValue)) onPasswordChange(newValue.text)
			}
		},
		isError = isError || error != null,
		// Always present, with or without a message: the line under the field is reserved either way, so the
		// form does not get tighter or jump when the message comes and goes.
		supportingText = { if (error != null) Text(modifier = errorModifier, text = error) },
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
			capitalization = KeyboardCapitalization.None,
			autoCorrectEnabled = false,
			imeAction = imeAction,
			keyboardType = KeyboardType.Password
		),
		keyboardActions = keyboardActions,
		singleLine = true
	)
}
