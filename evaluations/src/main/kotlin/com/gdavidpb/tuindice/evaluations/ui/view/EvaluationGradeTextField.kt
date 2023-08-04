package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DonutLarge
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.utils.MAX_EVALUATION_GRADE
import com.gdavidpb.tuindice.evaluations.utils.MIN_EVALUATION_GRADE

@Composable
fun EvaluationGradeTextField(
	modifier: Modifier = Modifier,
	grade: Double?,
	maxGrade: Double?,
	onGradeChanged: (grade: Double?) -> Unit,
	error: String? = null
) {
	OutlinedTextField(
		modifier = modifier,
		value = grade?.formatGrade().orEmpty(),
		onValueChange = { newValue ->
			val newGrade = newValue.toDoubleOrNull()

			val isValid = newGrade != null &&
					newGrade in MIN_EVALUATION_GRADE..(maxGrade ?: MAX_EVALUATION_GRADE)

			val isEmpty = (newGrade != null && newGrade.isNaN())
					|| newValue.isEmpty()

			if (isValid || isEmpty)
				onGradeChanged(newGrade)
		},
		suffix = {
			Text(
				text = stringResource(R.string.evaluation_max_grade, maxGrade ?: 0.0)
			)
		},
		isError = error != null,
		supportingText = {
			if (error != null) Text(text = error)
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