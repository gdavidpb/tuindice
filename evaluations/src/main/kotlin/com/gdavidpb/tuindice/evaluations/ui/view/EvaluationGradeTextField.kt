package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DonutLarge
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
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.utils.MIN_EVALUATION_GRADE

@Composable
fun EvaluationGradeTextField(
	modifier: Modifier = Modifier,
	grade: Double,
	maxGrade: Double,
	onGradeChanged: (grade: Double) -> Unit,
	error: String? = null
) {
	val textField = remember { mutableStateOf(TextFieldValue(grade.formatGrade())) }
	val supportingText = remember { mutableStateOf(error) }

	OutlinedTextField(
		modifier = modifier,
		value = textField.value,
		onValueChange = { newValue ->
			val newGrade = newValue.text.toDoubleOrNull()
			val isValid = newGrade != null && newGrade in MIN_EVALUATION_GRADE..maxGrade
			val isEmpty = newGrade != null && newGrade.isNaN() || newValue.text.isEmpty()

			if (isValid || isEmpty) {
				supportingText.value = null

				textField.value = newValue

				onGradeChanged(newGrade ?: 0.0)
			}
		},
		suffix = {
			Text(
				text = stringResource(R.string.evaluation_max_grade, maxGrade)
			)
		},
		isError = supportingText.value != null,
		supportingText = {
			val text = supportingText.value

			if (text != null) Text(text)
		},
		placeholder = {
			Text(
				text = stringResource(R.string.hint_evaluation_grade)
			)
		},
		leadingIcon = {
			Icon(
				imageVector = Icons.Outlined.DonutLarge,
				contentDescription = null
			)
		},
		keyboardOptions = KeyboardOptions(
			imeAction = ImeAction.Next,
			keyboardType = KeyboardType.Decimal
		),
		singleLine = true
	)
}