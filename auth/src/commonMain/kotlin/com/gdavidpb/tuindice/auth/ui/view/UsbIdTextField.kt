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
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.auth.domain.model.SignInIdentifierMode
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.ui.view.PulsingIconHalo

private const val USB_ID_MAX_DIGITS = 7
private val identifierModeTogglePulseSize = 32.dp

@Composable
fun UsbIdTextField(
	modifier: Modifier = Modifier,
	labelText: String,
	placeholderText: String,
	error: String? = null,
	identifierMode: SignInIdentifierMode = SignInIdentifierMode.UsbId,
	toggleContentDescription: String,
	showTogglePulse: Boolean,
	onUsbIdChange: (usbId: String) -> Unit,
	onIdentifierModeToggle: () -> Unit,
	usbId: String,
	keyboardActions: KeyboardActions = KeyboardActions.Default
) {
	val textField = remember {
		mutableStateOf(
			TextFieldValue(
				text = usbId,
				selection = TextRange(usbId.length)
			)
		)
	}
	val supportingText = remember { mutableStateOf(error) }
	val digitsOnlyRegex = remember { "\\D+".toRegex() }
	val shouldShowTogglePulse =
		showTogglePulse && usbId.isEmpty() && identifierMode == SignInIdentifierMode.UsbId

	LaunchedEffect(usbId, identifierMode) {
		if (textField.value.text != usbId) {
			val selectionEnd = textField.value.selection.end.coerceAtMost(usbId.length)

			textField.value = TextFieldValue(
				text = usbId,
				selection = TextRange(selectionEnd)
			)
		}
	}

	LaunchedEffect(error) {
		supportingText.value = error
	}

	OutlinedTextField(
		modifier = modifier.testTag(AuthUiTags.UsbIdTextField),
		value = textField.value,
		onValueChange = { newValue ->
			val previousText = textField.value.text
			if (newValue.text == previousText) {
				textField.value = newValue
				return@OutlinedTextField
			}

			val s = when (identifierMode) {
				SignInIdentifierMode.UsbId -> newValue.text
					.replace(digitsOnlyRegex, "")
					.let { digitsOnly ->
						StringBuilder(digitsOnly).apply {
							val atLeast2Digits = length >= 2
							val newContainsDash = newValue.text.elementAtOrNull(2) == '-'
							val oldContainsDash = previousText.elementAtOrNull(2) == '-'

							if (atLeast2Digits && (!oldContainsDash || newContainsDash))
								insert(2, '-')
						}.toString()
					}

				SignInIdentifierMode.UsbEmail -> newValue.text
			}

			if (identifierMode == SignInIdentifierMode.UsbEmail || s.length <= USB_ID_MAX_DIGITS + 1) {
				textField.value = TextFieldValue(
					text = s,
					selection = TextRange(s.length)
				)
				supportingText.value = null
				onUsbIdChange(textField.value.text)
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
