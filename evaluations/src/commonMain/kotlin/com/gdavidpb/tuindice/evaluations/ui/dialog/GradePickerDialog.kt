package com.gdavidpb.tuindice.evaluations.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.exposeTestTagsAsResourceId
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.evaluations.ui.model.MIN_EVALUATION_GRADE
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationGradeWheelPicker
import com.gdavidpb.tuindice.evaluations.ui.view.SubjectCodeChip

@Composable
fun GradePickerDialog(
	title: String,
	evaluationName: String,
	subjectCode: String,
	acceptText: String,
	cancelText: String,
	selectedGrade: Double?,
	gradeRange: ClosedFloatingPointRange<Double>,
	onGradeChange: (grade: Double) -> Unit,
	onDismissRequest: () -> Unit,
	dismissOnConfirm: Boolean = true
) {
	val selectedGradeState = remember(selectedGrade, gradeRange.start, gradeRange.endInclusive) {
		mutableDoubleStateOf(selectedGrade ?: MIN_EVALUATION_GRADE)
	}
	val subjectColors = remember(subjectCode) {
		CourseCodeColorGenerator.fromCode(subjectCode)
	}

	AlertDialog(
		modifier = Modifier.exposeTestTagsAsResourceId(),
		title = {
			Column(modifier = Modifier.fillMaxWidth()) {
				Text(
					modifier = Modifier.testTag(EvaluationsUiTags.EvaluationDialogTitle),
					text = title
				)

				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(top = 4.dp)
						.testTag(EvaluationsUiTags.EvaluationDialogSubtitle),
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						modifier = Modifier.weight(1f, fill = false),
						text = evaluationName,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						style = MaterialTheme.typography.bodyMedium,
						maxLines = 2,
						overflow = TextOverflow.Ellipsis
					)

					Text(
						modifier = Modifier.padding(horizontal = 8.dp),
						text = "•",
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						style = MaterialTheme.typography.bodyMedium
					)

					SubjectCodeChip(
						modifier = Modifier.testTag(EvaluationsUiTags.EvaluationDialogSubjectCodeChip),
						subjectCode = subjectCode,
						containerColor = subjectColors.containerColor,
						contentColor = subjectColors.color
					)
				}
			}
		},
		text = {
			EvaluationGradeWheelPicker(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 4.dp, bottom = 0.dp)
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
					if (dismissOnConfirm) {
						onDismissRequest()
					}
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
