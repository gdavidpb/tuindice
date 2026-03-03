package com.gdavidpb.tuindice.evaluations.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.evaluations.ui.dialog.GradePickerDialog
import com.gdavidpb.tuindice.evaluations.ui.model.EvaluationGradeWheelPickerDefaults
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.*

@Composable
fun GradePickerContentDialog(
	selectedGrade: Double?,
	maxGrade: Double?,
	onGradeChange: (grade: Double) -> Unit,
	onDismissRequest: () -> Unit
) {
	GradePickerDialog(
		title = stringResource(Res.string.dialog_title_add_evaluation_grade),
		acceptText = stringResource(Res.string.accept),
		cancelText = stringResource(Res.string.cancel),
		selectedGrade = selectedGrade ?: maxGrade ?: MIN_EVALUATION_DIALOG_GRADE,
		gradeRange = MIN_EVALUATION_DIALOG_GRADE..(maxGrade ?: MAX_EVALUATION_DIALOG_GRADE),
		onGradeChange = onGradeChange,
		onDismissRequest = onDismissRequest
	)
}

@Composable
fun MaxGradePickerContentDialog(
	selectedGrade: Double?,
	onGradeChange: (grade: Double) -> Unit,
	onDismissRequest: () -> Unit
) {
	GradePickerDialog(
		title = stringResource(Res.string.dialog_title_add_evaluation_max_grade),
		acceptText = stringResource(Res.string.accept),
		cancelText = stringResource(Res.string.cancel),
		selectedGrade = selectedGrade ?: MAX_EVALUATION_DIALOG_GRADE,
		gradeRange = EvaluationGradeWheelPickerDefaults.GradeRange,
		onGradeChange = onGradeChange,
		onDismissRequest = onDismissRequest
	)
}

@Composable
fun EvaluationGradePickerContentDialog(
	selectedGrade: Double?,
	maxGrade: Double,
	onGradeChange: (grade: Double) -> Unit,
	onDismissRequest: () -> Unit
) {
	GradePickerDialog(
		title = stringResource(Res.string.dialog_title_edit_evaluation_grade),
		acceptText = stringResource(Res.string.accept),
		cancelText = stringResource(Res.string.cancel),
		selectedGrade = selectedGrade ?: maxGrade,
		gradeRange = MIN_EVALUATION_DIALOG_GRADE..maxGrade,
		onGradeChange = onGradeChange,
		onDismissRequest = onDismissRequest
	)
}

private const val MIN_EVALUATION_DIALOG_GRADE = 0.0
private const val MAX_EVALUATION_DIALOG_GRADE = 100.0
