package com.gdavidpb.tuindice.evaluations.ui.dialog

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.evaluations.ui.model.EvaluationGradeWheelPickerDefaults
import com.gdavidpb.tuindice.evaluations.ui.model.MAX_EVALUATION_GRADE
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.accept
import tuindice.evaluations.generated.resources.cancel
import tuindice.evaluations.generated.resources.dialog_title_add_evaluation_max_grade

private const val MIN_SELECTABLE_MAX_GRADE = 0.25

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
		selectedGrade = selectedGrade?.takeIf { it > 0.0 } ?: MAX_EVALUATION_GRADE,
		gradeRange = MIN_SELECTABLE_MAX_GRADE..EvaluationGradeWheelPickerDefaults.GradeRange.endInclusive,
		onGradeChange = onGradeChange,
		onDismissRequest = onDismissRequest,
		dismissOnConfirm = dismissOnConfirm
	)
}
