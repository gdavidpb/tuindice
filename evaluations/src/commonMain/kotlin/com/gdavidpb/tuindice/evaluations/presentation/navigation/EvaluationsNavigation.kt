package com.gdavidpb.tuindice.evaluations.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectBackResultWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectCurrentEntryValueWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.navigateBackWithResult
import com.gdavidpb.tuindice.evaluations.presentation.route.EvaluationRoute
import com.gdavidpb.tuindice.evaluations.presentation.route.EvaluationsRoute
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.evaluations.ui.screen.EvaluationGradePickerContentDialog
import com.gdavidpb.tuindice.evaluations.ui.screen.GradePickerContentDialog
import com.gdavidpb.tuindice.evaluations.ui.screen.MaxGradePickerContentDialog
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.evaluationsNavigation(
	navController: NavHostController,
	onNavigateToAddEvaluation: () -> Unit,
	onNavigateToEvaluation: (evaluationId: String) -> Unit,
	onNavigateToEvaluationGradePickerDialog: (evaluationId: String, grade: Double, maxGrade: Double) -> Unit,
	onNavigateToGradePickerDialog: (grade: Double?, maxGrade: Double?) -> Unit,
	onNavigateToMaxGradePickerDialog: (maxGrade: Double?) -> Unit,
	onNavigateToEvaluations: () -> Unit,
	onViewStateChanged: (ViewState) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	navigation<EvaluationsDestination.NavGraph>(startDestination = EvaluationsDestination.Evaluations) {
		composable<EvaluationsDestination.Evaluations> { backStackEntry ->
			val viewModel = koinViewModel<EvaluationsViewModel>(viewModelStoreOwner = backStackEntry)
			val viewState by viewModel.state.collectAsStateWithLifecycle()

			navController.CollectCurrentEntryValueWithLifecycle(
				backStackEntry = backStackEntry,
				value = viewState,
				onValue = onViewStateChanged
			)

			navController.CollectBackResultWithLifecycle<EvaluationsBackResult>(
				backStackEntry = backStackEntry
			) { result ->
				when (result) {
					is EvaluationsBackResult.SetEvaluationGrade ->
						viewModel.setEvaluationGradeAction(
							evaluationId = result.evaluationId,
							grade = result.grade
						)
				}
			}

			EvaluationsRoute(
				onNavigateToAddEvaluation = onNavigateToAddEvaluation,
				onNavigateToEvaluation = onNavigateToEvaluation,
				onNavigateToEvaluationGradePickerDialog = onNavigateToEvaluationGradePickerDialog,
				showSnackBar = showSnackBar,
				viewModel = viewModel
			)
		}

		composable<EvaluationsDestination.Evaluation> { backStackEntry ->
			val args = backStackEntry.toRoute<EvaluationsDestination.Evaluation>()
			val viewModel = koinViewModel<EvaluationViewModel>(viewModelStoreOwner = backStackEntry)
			val viewState by viewModel.state.collectAsStateWithLifecycle()

			navController.CollectCurrentEntryValueWithLifecycle(
				backStackEntry = backStackEntry,
				value = viewState,
				onValue = onViewStateChanged
			)

			navController.CollectBackResultWithLifecycle<EvaluationBackResult>(
				backStackEntry = backStackEntry
			) { result ->
				when (result) {
					is EvaluationBackResult.SetGrade ->
						viewModel.setGradeAction(result.grade)

					is EvaluationBackResult.SetMaxGrade ->
						viewModel.setMaxGradeAction(result.grade)
				}
			}

			EvaluationRoute(
				evaluationId = args.evaluationId,
				onNavigateToEvaluations = onNavigateToEvaluations,
				onNavigateToGradePickerDialog = onNavigateToGradePickerDialog,
				onNavigateToMaxGradePickerDialog = onNavigateToMaxGradePickerDialog,
				showSnackBar = showSnackBar,
				viewModel = viewModel
			)
		}

		dialog<EvaluationsDestination.GradePickerDialog> { backStackEntry ->
			val args = backStackEntry.toRoute<EvaluationsDestination.GradePickerDialog>()

			GradePickerContentDialog(
				selectedGrade = args.grade,
				maxGrade = args.maxGrade,
				onGradeChange = { grade ->
					navController.navigateBackWithResult<EvaluationBackResult>(
						EvaluationBackResult.SetGrade(grade)
					)
				},
				onDismissRequest = { navController.navigateUp() },
				dismissOnConfirm = false
			)
		}

		dialog<EvaluationsDestination.MaxGradePickerDialog> { backStackEntry ->
			val args = backStackEntry.toRoute<EvaluationsDestination.MaxGradePickerDialog>()

			MaxGradePickerContentDialog(
				selectedGrade = args.grade,
				onGradeChange = { grade ->
					navController.navigateBackWithResult<EvaluationBackResult>(
						EvaluationBackResult.SetMaxGrade(grade)
					)
				},
				onDismissRequest = { navController.navigateUp() },
				dismissOnConfirm = false
			)
		}

		dialog<EvaluationsDestination.EvaluationGradePickerDialog> { backStackEntry ->
			val args = backStackEntry.toRoute<EvaluationsDestination.EvaluationGradePickerDialog>()

			EvaluationGradePickerContentDialog(
				selectedGrade = args.grade,
				maxGrade = args.maxGrade,
				onGradeChange = { grade ->
					navController.navigateBackWithResult<EvaluationsBackResult>(
						EvaluationsBackResult.SetEvaluationGrade(
							evaluationId = args.evaluationId,
							grade = grade
						)
					)
				},
				onDismissRequest = { navController.navigateUp() },
				dismissOnConfirm = false
			)
		}
	}
}
