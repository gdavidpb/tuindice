package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.auth.domain.model.SignInIdentifierMode
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.ui.text.EditableTextFieldState
import com.gdavidpb.tuindice.base.ui.view.PulsingIconHalo

private val identifierModeTogglePulseSize = 32.dp

/** [isWaiting]: see [PasswordTextField]. */
@Composable
fun UsbIdTextField(
	modifier: Modifier = Modifier,
	labelText: String,
	placeholderText: String,
	error: String? = null,
	identifierMode: SignInIdentifierMode = SignInIdentifierMode.UsbId,
	toggleContentDescription: String,
	showTogglePulse: Boolean,
	isWaiting: Boolean,
	onUsbIdChange: (usbId: String) -> Unit,
	onIdentifierModeToggle: () -> Unit,
	usbId: String,
	keyboardActions: KeyboardActions = KeyboardActions.Default
) {
	val field = remember { EditableTextFieldState(usbId, identifierMode to isWaiting) }
	val supportingText = remember { mutableStateOf(error) }
	val shouldShowTogglePulse =
		showTogglePulse && usbId.isEmpty() && identifierMode == SignInIdentifierMode.UsbId

	field.syncExternal(usbId, identifierMode to isWaiting)

	LaunchedEffect(error) {
		supportingText.value = error
	}

	OutlinedTextField(
		modifier = modifier
			.testTag(AuthUiTags.UsbIdTextField)
			.semantics { contentType = ContentType.Username },
		value = field.value,
		onValueChange = { newValue ->
			when {
				// While the owner is busy the view model drops what it is told (see PasswordTextField).
				isWaiting -> Unit

				newValue.text == field.value.text -> field.edit(newValue)

				else -> {
					// An @ means an email (autofill, paste): the mask would discard it, so it goes
					// through whole and the owner switches the mode, which this field then adopts.
					val edited = when {
						identifierMode == SignInIdentifierMode.UsbId && '@' !in newValue.text ->
							newValue.toMaskedUsbId(previous = field.value)

						else -> newValue
					}

					if (field.edit(edited)) {
						supportingText.value = null
						onUsbIdChange(edited.text)
					}
				}
			}
		},
		isError = supportingText.value != null,
			supportingText = {
				val text = supportingText.value

				if (text != null) Text(text)
			},
		label = { Text(text = labelText) },
		placeholder = { Text(text = placeholderText) },
		leadingIcon = {
			Icon(
				imageVector = Icons.Outlined.Person,
				contentDescription = null
			)
		},
		trailingIcon = {
			Box(
				modifier = Modifier.size(48.dp),
				contentAlignment = Alignment.Center
			) {
				if (shouldShowTogglePulse) {
					PulsingIconHalo(
						color = MaterialTheme.colorScheme.surfaceTint,
						size = identifierModeTogglePulseSize,
						testTag = AuthUiTags.IdentifierModeTogglePulse
					)
				}

				IconButton(
					modifier = Modifier.testTag(AuthUiTags.IdentifierModeToggle),
					onClick = onIdentifierModeToggle
				) {
					Icon(
						imageVector = when (identifierMode) {
							SignInIdentifierMode.UsbId -> Icons.Outlined.Email
							SignInIdentifierMode.UsbEmail -> Icons.Outlined.Badge
						},
						contentDescription = toggleContentDescription
					)
				}
			}
		},
		keyboardOptions = KeyboardOptions(
			capitalization = KeyboardCapitalization.None,
			autoCorrectEnabled = false,
			imeAction = ImeAction.Next,
			keyboardType = when (identifierMode) {
				SignInIdentifierMode.UsbId -> KeyboardType.Number
				SignInIdentifierMode.UsbEmail -> KeyboardType.Email
			}
		),
		keyboardActions = keyboardActions,
		singleLine = true
	)
}
