package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.gdavidpb.tuindice.evaluations.R

private const val MAX_TEXT_LENGTH_NAME = 32

@Composable
fun EvaluationNameTextField(
	modifier: Modifier = Modifier,
	name: String?,
	onNameChanged: (name: String) -> Unit,
	error: String? = null
) {
	OutlinedTextField(
		modifier = modifier,
		value = name.orEmpty(),
		onValueChange = { newValue ->
			if (newValue.length <= MAX_TEXT_LENGTH_NAME) onNameChanged(newValue)
		},
		isError = error != null,
		supportingText = {
			if (error != null) Text(text = error)
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