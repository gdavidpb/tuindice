package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.gdavidpb.tuindice.auth.ui.AuthUiTags

private const val USB_ID_MAX_DIGITS = 7

@Composable
fun UsbIdTextField(
	modifier: Modifier = Modifier,
	labelText: String,
	error: String? = null,
	onUsbIdChange: (usbId: String) -> Unit,
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

	LaunchedEffect(usbId) {
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

			val s = newValue.text
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

			if (s.length <= USB_ID_MAX_DIGITS + 1) {
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
		leadingIcon = {
			Icon(
				imageVector = Icons.Filled.Person,
				contentDescription = null
			)
		},
		keyboardOptions = KeyboardOptions(
			imeAction = ImeAction.Next,
			keyboardType = KeyboardType.Number
		),
		keyboardActions = keyboardActions,
		singleLine = true
	)
}
