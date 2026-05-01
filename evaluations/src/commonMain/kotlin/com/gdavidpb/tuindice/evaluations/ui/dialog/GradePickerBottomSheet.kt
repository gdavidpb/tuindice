package com.gdavidpb.tuindice.evaluations.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.evaluations.ui.model.MIN_EVALUATION_GRADE
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationGradeWheelPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradePickerBottomSheet(
	title: String,
	contextText: String,
	acceptText: String,
	cancelText: String,
	selectedGrade: Double?,
	gradeRange: ClosedFloatingPointRange<Double>,
	onGradeChange: (grade: Double) -> Unit,
	onDismissRequest: () -> Unit,
	dismissOnConfirm: Boolean = true
) {
	val sheetState = rememberModalBottomSheetState()
	val selectedGradeState = remember(selectedGrade, gradeRange.start, gradeRange.endInclusive) {
		mutableDoubleStateOf(selectedGrade ?: MIN_EVALUATION_GRADE)
	}

	ModalBottomSheet(
		sheetState = sheetState,
		onDismissRequest = onDismissRequest
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 24.dp)
		) {
			Text(
				modifier = Modifier.testTag(EvaluationsUiTags.EvaluationDialogTitle),
				text = title,
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Bold
			)

			Text(
				modifier = Modifier.padding(top = 4.dp),
				text = contextText,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)

			EvaluationGradeWheelPicker(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 24.dp)
					.testTag(EvaluationsUiTags.EvaluationGradeWheelPicker),
				grade = selectedGrade ?: MIN_EVALUATION_GRADE,
				gradeRange = gradeRange,
				onGradeChange = { grade ->
					selectedGradeState.doubleValue = grade
				},
				textStyle = MaterialTheme.typography.titleLarge
			)

			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 24.dp, bottom = 16.dp)
			) {
				TextButton(
					modifier = Modifier
						.weight(1f)
						.testTag(EvaluationsUiTags.EvaluationDialogDismissButton),
					onClick = onDismissRequest
				) {
					Text(text = cancelText)
				}

				TextButton(
					modifier = Modifier
						.weight(1f)
						.testTag(EvaluationsUiTags.EvaluationDialogConfirmButton),
					onClick = {
						onGradeChange(selectedGradeState.doubleValue)
						if (dismissOnConfirm) {
							onDismissRequest()
						}
					}
				) {
					Text(text = acceptText)
				}
			}
		}
	}
}
