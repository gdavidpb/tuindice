package com.gdavidpb.tuindice.evaluations.ui.dialog

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.evaluations.ui.model.MIN_EVALUATION_GRADE
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.accept
import tuindice.evaluations.generated.resources.cancel
import tuindice.evaluations.generated.resources.dialog_title_edit_evaluation_grade

@Composable
fun EvaluationGradePickerContentDialog(
	evaluationName: String,
	selectedGrade: Double?,
	maxGrade: Double,
	onGradeChange: (grade: Double) -> Unit,
	onDismissRequest: () -> Unit,
	dismissOnConfirm: Boolean = true
) {
	GradePickerBottomSheet(
		title = stringResource(Res.string.dialog_title_edit_evaluation_grade),
		contextText = evaluationName,
		acceptText = stringResource(Res.string.accept),
		cancelText = stringResource(Res.string.cancel),
		selectedGrade = selectedGrade ?: maxGrade,
		gradeRange = MIN_EVALUATION_GRADE..maxGrade,
		onGradeChange = onGradeChange,
		onDismissRequest = onDismissRequest,
		dismissOnConfirm = dismissOnConfirm
	)
}
