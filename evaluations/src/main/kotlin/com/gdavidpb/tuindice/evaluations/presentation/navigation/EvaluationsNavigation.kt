package com.gdavidpb.tuindice.evaluations.presentation.navigation

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.route.EvaluationRoute
import com.gdavidpb.tuindice.evaluations.presentation.route.EvaluationsRoute
import com.gdavidpb.tuindice.evaluations.ui.dialog.GradePickerDialog
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.EvaluationGradeWheelPickerDefaults
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.utils.MAX_EVALUATION_GRADE
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.utils.MIN_EVALUATION_GRADE

fun NavGraphBuilder.evaluationsNavigation(
	onNavigateToAddEvaluation: () -> Unit,
	onNavigateToEvaluation: (evaluationId: String) -> Unit,
	onNavigateToEvaluationGradePickerDialog: (evaluationId: String, grade: Double, maxGrade: Double) -> Unit,
	onNavigateToGradePickerDialog: (grade: Double?, maxGrade: Double?) -> Unit,
	onNavigateToMaxGradePickerDialog: (maxGrade: Double?) -> Unit,
	onNavigateToEvaluations: () -> Unit,
	onSetGrade: (grade: Double) -> Unit,
	onSetMaxGrade: (grade: Double) -> Unit,
	onSetEvaluationGrade: (evaluationId: String, grade: Double) -> Unit,
	onDismissRequest: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	navigation<EvaluationsDestination.NavGraph>(startDestination = EvaluationsDestination.Evaluations) {
		composable<EvaluationsDestination.Evaluations> {
			EvaluationsRoute(
				onNavigateToAddEvaluation = onNavigateToAddEvaluation,
				onNavigateToEvaluation = onNavigateToEvaluation,
				onNavigateToEvaluationGradePickerDialog = onNavigateToEvaluationGradePickerDialog,
				showSnackBar = showSnackBar
			)
		}

		composable<EvaluationsDestination.Evaluation> { backStackEntry ->
			val args = backStackEntry.toRoute<EvaluationsDestination.Evaluation>()

			EvaluationRoute(
				evaluationId = args.evaluationId,
				onNavigateToEvaluations = onNavigateToEvaluations,
				onNavigateToGradePickerDialog = onNavigateToGradePickerDialog,
				onNavigateToMaxGradePickerDialog = onNavigateToMaxGradePickerDialog,
				showSnackBar = showSnackBar
			)
		}

		dialog<EvaluationsDestination.GradePickerDialog> { backStackEntry ->
			val args = backStackEntry.toRoute<EvaluationsDestination.GradePickerDialog>()

			GradePickerDialog(
				title = stringResource(R.string.dialog_title_add_evaluation_grade),
				selectedGrade = args.grade,
				gradeRange = MIN_EVALUATION_GRADE..(args.maxGrade
					?: MAX_EVALUATION_GRADE),
				onGradeChange = onSetGrade,
				onDismissRequest = onDismissRequest
			)
		}

		dialog<EvaluationsDestination.MaxGradePickerDialog> { backStackEntry ->
			val args = backStackEntry.toRoute<EvaluationsDestination.MaxGradePickerDialog>()

			GradePickerDialog(
				title = stringResource(id = R.string.dialog_title_add_evaluation_max_grade),
				selectedGrade = args.grade,
				gradeRange = EvaluationGradeWheelPickerDefaults.GradeRange,
				onGradeChange = onSetMaxGrade,
				onDismissRequest = onDismissRequest
			)
		}

		dialog<EvaluationsDestination.EvaluationGradePickerDialog> { backStackEntry ->
			val args = backStackEntry.toRoute<EvaluationsDestination.EvaluationGradePickerDialog>()

			GradePickerDialog(
				title = stringResource(id = R.string.dialog_title_add_evaluation_grade),
				selectedGrade = args.grade,
				gradeRange = MIN_EVALUATION_GRADE..args.maxGrade,
				onGradeChange = { grade ->
					onSetEvaluationGrade(
						args.evaluationId,
						grade
					)
				},
				onDismissRequest = onDismissRequest
			)
		}
	}
}