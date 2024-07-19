package com.gdavidpb.tuindice.evaluations.presentation.navigation

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.viewModel
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.route.EvaluationRoute
import com.gdavidpb.tuindice.evaluations.presentation.route.EvaluationsRoute
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.evaluations.ui.dialog.GradePickerDialog
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.EvaluationGradeWheelPickerDefaults
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.utils.MAX_EVALUATION_GRADE
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.utils.MIN_EVALUATION_GRADE

fun NavGraphBuilder.evaluationsNavigation(
	navController: NavController,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	navigation<EvaluationsDestination.NavGraph>(startDestination = EvaluationsDestination.Evaluations) {
		composable<EvaluationsDestination.Evaluations> {
			EvaluationsRoute(
				onNavigateToAddEvaluation = {
					navController.navigate(
						EvaluationsDestination.Evaluation(
							evaluationId = null
						)
					)
				},
				onNavigateToEvaluation = { evaluationId ->
					navController.navigate(
						EvaluationsDestination.Evaluation(
							evaluationId = evaluationId
						)
					)
				},
				onNavigateToEvaluationGradePickerDialog = { evaluationId, grade, maxGrade ->
					navController.navigate(
						EvaluationsDestination.EvaluationGradePickerDialog(
							evaluationId = evaluationId,
							grade = grade.toFloat(),
							maxGrade = maxGrade.toFloat()
						)
					)
				},
				showSnackBar = showSnackBar
			)
		}

		composable<EvaluationsDestination.Evaluation> { backStackEntry ->
			val args = backStackEntry.toRoute<EvaluationsDestination.Evaluation>()

			EvaluationRoute(
				evaluationId = args.evaluationId,
				onNavigateToEvaluations = {
					navController.navigate(EvaluationsDestination.Evaluations)
				},
				onNavigateToGradePickerDialog = { grade, maxGrade ->
					navController.navigate(
						EvaluationsDestination.GradePickerDialog(
							grade = grade?.toFloat(),
							maxGrade = maxGrade?.toFloat()
						)
					)
				},
				onNavigateToMaxGradePickerDialog = { maxGrade ->
					navController.navigate(
						EvaluationsDestination.MaxGradePickerDialog(
							grade = maxGrade?.toFloat()
						)
					)
				},
				showSnackBar = showSnackBar
			)
		}

		dialog<EvaluationsDestination.GradePickerDialog> { backStackEntry ->
			val args = backStackEntry.toRoute<EvaluationsDestination.GradePickerDialog>()

			GradePickerDialog(
				title = stringResource(R.string.dialog_title_add_evaluation_grade),
				selectedGrade = args.grade?.toDouble(),
				gradeRange = MIN_EVALUATION_GRADE..(args.maxGrade?.toDouble() ?: MAX_EVALUATION_GRADE),
				onGradeChange = { grade ->
					navController
						.viewModel<EvaluationViewModel>()
						?.setGradeAction(
							grade = grade
						)
				},
				onDismissRequest = {
					navController.navigateUp()
				}
			)
		}

		dialog<EvaluationsDestination.MaxGradePickerDialog> { backStackEntry ->
			val args = backStackEntry.toRoute<EvaluationsDestination.MaxGradePickerDialog>()

			GradePickerDialog(
				title = stringResource(id = R.string.dialog_title_add_evaluation_max_grade),
				selectedGrade = args.grade?.toDouble(),
				gradeRange = EvaluationGradeWheelPickerDefaults.GradeRange,
				onGradeChange = { grade ->
					navController
						.viewModel<EvaluationViewModel>()
						?.setMaxGradeAction(
							grade = grade
						)
				},
				onDismissRequest = {
					navController.navigateUp()
				}
			)
		}

		dialog<EvaluationsDestination.EvaluationGradePickerDialog> { backStackEntry ->
			val args = backStackEntry.toRoute<EvaluationsDestination.EvaluationGradePickerDialog>()

			GradePickerDialog(
				title = stringResource(id = R.string.dialog_title_add_evaluation_grade),
				selectedGrade = args.grade.toDouble(),
				gradeRange = MIN_EVALUATION_GRADE..args.maxGrade.toDouble(),
				onGradeChange = { grade ->
					navController
						.viewModel<EvaluationsViewModel>()
						?.setEvaluationGradeAction(
							evaluationId = args.evaluationId,
							grade = grade
						)
				},
				onDismissRequest = {
					navController.navigateUp()
				}
			)
		}
	}
}