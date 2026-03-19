package com.gdavidpb.tuindice.evaluations.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
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
	onDismissRequest: () -> Unit,
	onViewStateChanged: (ViewState) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	navigation<EvaluationsDestination.NavGraph>(startDestination = EvaluationsDestination.Evaluations) {
		composable<EvaluationsDestination.Evaluations> { backStackEntry ->
			val viewModel = koinViewModel<EvaluationsViewModel>(viewModelStoreOwner = backStackEntry)
			val viewState by viewModel.state.collectAsStateWithLifecycle()

			LaunchedEffect(viewState) {
				onViewStateChanged(viewState)
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

			LaunchedEffect(viewState) {
				onViewStateChanged(viewState)
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
			val parentEntry = navController.previousBackStackEntry ?: return@dialog
			val viewModel = koinViewModel<EvaluationViewModel>(viewModelStoreOwner = parentEntry)

			GradePickerContentDialog(
				selectedGrade = args.grade,
				maxGrade = args.maxGrade,
				onGradeChange = viewModel::setGradeAction,
				onDismissRequest = onDismissRequest
			)
		}

		dialog<EvaluationsDestination.MaxGradePickerDialog> { backStackEntry ->
			val args = backStackEntry.toRoute<EvaluationsDestination.MaxGradePickerDialog>()
			val parentEntry = navController.previousBackStackEntry ?: return@dialog
			val viewModel = koinViewModel<EvaluationViewModel>(viewModelStoreOwner = parentEntry)

			MaxGradePickerContentDialog(
				selectedGrade = args.grade,
				onGradeChange = viewModel::setMaxGradeAction,
				onDismissRequest = onDismissRequest
			)
		}

		dialog<EvaluationsDestination.EvaluationGradePickerDialog> { backStackEntry ->
			val args = backStackEntry.toRoute<EvaluationsDestination.EvaluationGradePickerDialog>()
			val parentEntry = navController.previousBackStackEntry ?: return@dialog
			val viewModel = koinViewModel<EvaluationsViewModel>(viewModelStoreOwner = parentEntry)

			EvaluationGradePickerContentDialog(
				selectedGrade = args.grade,
				maxGrade = args.maxGrade,
				onGradeChange = { grade ->
					viewModel.setEvaluationGradeAction(
						evaluationId = args.evaluationId,
						grade = grade
					)
				},
				onDismissRequest = onDismissRequest
			)
		}
	}
}
