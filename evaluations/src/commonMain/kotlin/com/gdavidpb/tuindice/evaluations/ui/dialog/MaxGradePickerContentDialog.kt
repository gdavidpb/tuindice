package com.gdavidpb.tuindice.evaluations.ui.dialog

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.evaluations.ui.model.EvaluationGradeWheelPickerDefaults
import com.gdavidpb.tuindice.evaluations.ui.model.MIN_EVALUATION_GRADE
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.accept
import tuindice.evaluations.generated.resources.cancel
import tuindice.evaluations.generated.resources.dialog_title_add_evaluation_max_grade

@Composable
fun MaxGradePickerContentDialog(
	selectedGrade: Double?,
	onGradeChange: (grade: Double) -> Unit,
	onDismissRequest: () -> Unit,
	dismissOnConfirm: Boolean = true
) {
	GradePickerDialog(
		title = stringResource(Res.string.dialog_title_add_evaluation_max_grade),
		acceptText = stringResource(Res.string.accept),
		cancelText = stringResource(Res.string.cancel),
		selectedGrade = selectedGrade ?: MIN_EVALUATION_GRADE,
		gradeRange = EvaluationGradeWheelPickerDefaults.GradeRange,
		onGradeChange = onGradeChange,
		onDismissRequest = onDismissRequest,
		dismissOnConfirm = dismissOnConfirm
	)
}
