package com.gdavidpb.tuindice.login.ui.view

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.gdavidpb.tuindice.login.R

@Composable
fun UsbIdTextField(
	modifier: Modifier = Modifier,
	usbId: String,
	error: String? = null,
	onValueChange: (usbId: String) -> Unit,
	keyboardActions: KeyboardActions = KeyboardActions.Default
) {
	val textField = remember { mutableStateOf(TextFieldValue(usbId)) }
	val supportingText = remember { mutableStateOf(error) }
	val digitsOnlyRegex = remember { "\\D+".toRegex() }

	OutlinedTextField(
		modifier = modifier,
		value = textField.value,
		onValueChange = { newValue ->
			val s = newValue.text
				.replace(digitsOnlyRegex, "")
				.let { digitsOnly ->
					StringBuilder(digitsOnly).apply {
						val atLeast2Digits = length >= 2
						val newContainsDash = newValue.text.elementAtOrNull(2) == '-'
						val oldContainsDash = textField.value.text.elementAtOrNull(2) == '-'

						if (atLeast2Digits && (!oldContainsDash || newContainsDash))
							insert(2, '-')
					}.toString()
				}

			if (s.length <= 8) {
				textField.value = TextFieldValue(
					text = s,
					selection = TextRange(s.length)
				)

				supportingText.value = null

				onValueChange(textField.value.text)
			}
		},
		isError = supportingText.value != null,
		supportingText = {
			val text = supportingText.value

			if (text != null) Text(text)
		},
		label = { Text(text = stringResource(R.string.hint_usb_id)) },
		leadingIcon = {
			Icon(
				imageVector = Icons.Outlined.Person,
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