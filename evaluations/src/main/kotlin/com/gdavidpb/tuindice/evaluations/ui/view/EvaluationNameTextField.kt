package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.gdavidpb.tuindice.evaluations.R

@Composable
fun EvaluationNameTextField(
	modifier: Modifier = Modifier,
	name: String?,
	onNameChanged: (name: String) -> Unit,
	error: String? = null
) {
	val textField = remember { mutableStateOf(TextFieldValue(name.orEmpty())) }
	val supportingText = remember { mutableStateOf(error) }

	OutlinedTextField(
		modifier = modifier,
		value = textField.value,
		onValueChange = { newValue ->
			supportingText.value = null

			textField.value = newValue

			onNameChanged(textField.value.text)
		},
		isError = supportingText.value != null,
		supportingText = {
			val text = supportingText.value

			if (text != null) Text(text)
		},
		placeholder = {
			Text(
				text = stringResource(R.string.hint_evaluation_name)
			)
		},
		leadingIcon = {
			Icon(
				imageVector = Icons.Outlined.Label,
				contentDescription = null
			)
		},
		keyboardOptions = KeyboardOptions(
			imeAction = ImeAction.Next,
			keyboardType = KeyboardType.Text
		),
		singleLine = true
	)
}