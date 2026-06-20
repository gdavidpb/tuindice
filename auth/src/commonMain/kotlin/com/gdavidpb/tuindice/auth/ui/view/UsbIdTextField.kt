package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
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
import com.gdavidpb.tuindice.auth.domain.model.SignInIdentifierMode
import com.gdavidpb.tuindice.auth.ui.AuthUiTags

private const val USB_ID_MAX_DIGITS = 7

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsbIdTextField(
	modifier: Modifier = Modifier,
	labelText: String,
	placeholderText: String,
	error: String? = null,
	identifierMode: SignInIdentifierMode = SignInIdentifierMode.UsbId,
	toggleContentDescription: String,
	tooltipText: String,
	showTooltip: Boolean,
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
	val tooltipState = rememberTooltipState(isPersistent = true)

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

	LaunchedEffect(showTooltip, usbId, identifierMode) {
		if (showTooltip && usbId.isEmpty() && identifierMode == SignInIdentifierMode.UsbId) {
			tooltipState.show()
		} else {
			tooltipState.dismiss()
		}
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
			TooltipBox(
				positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
					positioning = TooltipAnchorPosition.Below
				),
				tooltip = {
					PlainTooltip(
						modifier = Modifier.testTag(AuthUiTags.IdentifierModeTooltip)
					) {
						Text(text = tooltipText)
					}
				},
				state = tooltipState,
				enableUserInput = false
			) {
				IconButton(
					modifier = Modifier.testTag(AuthUiTags.IdentifierModeToggle),
					onClick = {
						tooltipState.dismiss()
						onIdentifierModeToggle()
					}
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
