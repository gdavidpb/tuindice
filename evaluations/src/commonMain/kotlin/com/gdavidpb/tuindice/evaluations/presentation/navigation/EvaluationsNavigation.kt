package com.gdavidpb.tuindice.evaluations.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navigation
import androidx.navigation.toRoute
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.evaluations.presentation.route.EvaluationRoute
import com.gdavidpb.tuindice.evaluations.presentation.route.EvaluationsRoute
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import org.koin.compose.koinInject

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
	onViewStateChanged: (ViewState) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	gradePickerDialogContent: @Composable (
		selectedGrade: Double?,
		maxGrade: Double?,
		onGradeChange: (grade: Double) -> Unit,
		onDismissRequest: () -> Unit
	) -> Unit,
	maxGradePickerDialogContent: @Composable (
		selectedGrade: Double?,
		onGradeChange: (grade: Double) -> Unit,
		onDismissRequest: () -> Unit
	) -> Unit,
	evaluationGradePickerDialogContent: @Composable (
		selectedGrade: Double?,
		maxGrade: Double,
		onGradeChange: (grade: Double) -> Unit,
		onDismissRequest: () -> Unit
	) -> Unit
) {
	navigation<EvaluationsDestination.NavGraph>(startDestination = EvaluationsDestination.Evaluations) {
		composable<EvaluationsDestination.Evaluations> {
			val viewModel = koinInject<EvaluationsViewModel>()
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
			val viewModel = koinInject<EvaluationViewModel>()
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

			gradePickerDialogContent(args.grade, args.maxGrade, onSetGrade, onDismissRequest)
		}

		dialog<EvaluationsDestination.MaxGradePickerDialog> { backStackEntry ->
			val args = backStackEntry.toRoute<EvaluationsDestination.MaxGradePickerDialog>()

			maxGradePickerDialogContent(args.grade, onSetMaxGrade, onDismissRequest)
		}

		dialog<EvaluationsDestination.EvaluationGradePickerDialog> { backStackEntry ->
			val args = backStackEntry.toRoute<EvaluationsDestination.EvaluationGradePickerDialog>()

			evaluationGradePickerDialogContent(
				args.grade,
				args.maxGrade,
				{ grade -> onSetEvaluationGrade(args.evaluationId, grade) },
				onDismissRequest
			)
		}
	}
}
