package com.gdavidpb.tuindice.evaluations.ui.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationGradeWheelPicker
import com.gdavidpb.tuindice.evaluations.ui.model.MIN_EVALUATION_GRADE

@Composable
fun GradePickerDialog(
	title: String,
	acceptText: String,
	cancelText: String,
	selectedGrade: Double?,
	gradeRange: ClosedFloatingPointRange<Double>,
	onGradeChange: (grade: Double) -> Unit,
	onDismissRequest: () -> Unit
) {
	val selectedGradeState = remember {
		mutableDoubleStateOf(selectedGrade ?: MIN_EVALUATION_GRADE)
	}

	AlertDialog(
		title = {
			Text(
				modifier = Modifier.testTag(EvaluationsUiTags.EvaluationDialogTitle),
				text = title
			)
		},
		text = {
			EvaluationGradeWheelPicker(
				modifier = Modifier
					.fillMaxWidth()
					.testTag(EvaluationsUiTags.EvaluationGradeWheelPicker),
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
				modifier = Modifier.testTag(EvaluationsUiTags.EvaluationDialogConfirmButton),
				onClick = {
					onGradeChange(selectedGradeState.doubleValue)
					onDismissRequest()
				}
			) {
				Text(text = acceptText)
			}
		},
		dismissButton = {
			TextButton(
				modifier = Modifier.testTag(EvaluationsUiTags.EvaluationDialogDismissButton),
				onClick = {
					onDismissRequest()
				}
			) {
				Text(text = cancelText)
			}
		}
	)
}
