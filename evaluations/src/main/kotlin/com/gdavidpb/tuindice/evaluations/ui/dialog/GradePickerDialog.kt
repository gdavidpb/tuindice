package com.gdavidpb.tuindice.evaluations.ui.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DonutLarge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.EvaluationGradeWheelPicker
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.utils.MIN_EVALUATION_GRADE

@Composable
fun GradePickerDialog(
	title: String,
	selectedGrade: Double?,
	gradeRange: ClosedFloatingPointRange<Double>,
	onGradeChange: (grade: Double) -> Unit,
	onDismissRequest: () -> Unit
) {
	val selectedGradeState = remember {
		mutableDoubleStateOf(selectedGrade ?: MIN_EVALUATION_GRADE)
	}

	AlertDialog(
		icon = {
			Icon(
				imageVector = Icons.Outlined.DonutLarge,
				contentDescription = null
			)
		},
		title = {
			Text(text = title)
		},
		text = {
			EvaluationGradeWheelPicker(
				modifier = Modifier
					.fillMaxWidth(),
				grade = selectedGrade ?: MIN_EVALUATION_GRADE,
				gradeRange = gradeRange,
				onGradeChange = { grade ->
					selectedGradeState.doubleValue = grade
				},
				textStyle = MaterialTheme.typography.titleLarge
			)
		},
		onDismissRequest = {
			onDismissRequest()
		},
		confirmButton = {
			TextButton(
				onClick = {
					onGradeChange(selectedGradeState.doubleValue)
					onDismissRequest()
				}
			) {
				Text(
					text = stringResource(id = R.string.accept)
				)
			}
		},
		dismissButton = {
			TextButton(
				onClick = {
					onDismissRequest()
				}
			) {
				Text(
					text = stringResource(id = R.string.cancel)
				)
			}
		}
	)
}