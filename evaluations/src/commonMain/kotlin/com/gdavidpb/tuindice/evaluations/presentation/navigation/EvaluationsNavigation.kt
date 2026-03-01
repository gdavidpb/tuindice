package com.gdavidpb.tuindice.evaluations.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.evaluations.presentation.mapper.EvaluationItemMappingProvider
import com.gdavidpb.tuindice.evaluations.presentation.route.EvaluationRoute
import com.gdavidpb.tuindice.evaluations.presentation.route.EvaluationsRoute
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.evaluations.ui.screen.EvaluationGradePickerContentDialog
import com.gdavidpb.tuindice.evaluations.ui.screen.GradePickerContentDialog
import com.gdavidpb.tuindice.evaluations.ui.screen.MaxGradePickerContentDialog
import org.koin.compose.koinInject

private const val GRADE_PICKER_RESULT_KEY = "grade_picker_result"
private const val MAX_GRADE_PICKER_RESULT_KEY = "max_grade_picker_result"
private const val EVALUATION_GRADE_PICKER_RESULT_KEY = "evaluation_grade_picker_result"

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
			val viewModel = koinInject<EvaluationsViewModel>()
			val mappingProvider = koinInject<EvaluationItemMappingProvider>()
			val viewState by viewModel.state.collectAsStateWithLifecycle()

			LaunchedEffect(viewState) {
				onViewStateChanged(viewState)
			}

			LaunchedEffect(backStackEntry, viewModel) {
				backStackEntry.savedStateHandle
					.getStateFlow<String?>(EVALUATION_GRADE_PICKER_RESULT_KEY, null)
					.collect { result ->
						if (result == null)
							return@collect

						val separatorIndex = result.indexOf('|')

						if (separatorIndex != -1) {
							val evaluationId = result.substring(0, separatorIndex)
							val grade = result.substring(separatorIndex + 1).toDoubleOrNull()

							if (grade != null) {
								viewModel.setEvaluationGradeAction(
									evaluationId = evaluationId,
									grade = grade
								)
							}
						}

						backStackEntry.savedStateHandle[EVALUATION_GRADE_PICKER_RESULT_KEY] = null
					}
			}

			EvaluationsRoute(
				onNavigateToAddEvaluation = onNavigateToAddEvaluation,
				onNavigateToEvaluation = onNavigateToEvaluation,
				onNavigateToEvaluationGradePickerDialog = onNavigateToEvaluationGradePickerDialog,
				showSnackBar = showSnackBar,
				mappingProvider = mappingProvider,
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

			LaunchedEffect(backStackEntry, viewModel) {
				backStackEntry.savedStateHandle
					.getStateFlow<Double?>(GRADE_PICKER_RESULT_KEY, null)
					.collect { grade ->
						if (grade == null)
							return@collect

						viewModel.setGradeAction(grade = grade)
						backStackEntry.savedStateHandle[GRADE_PICKER_RESULT_KEY] = null
					}
			}

			LaunchedEffect(backStackEntry, viewModel) {
				backStackEntry.savedStateHandle
					.getStateFlow<Double?>(MAX_GRADE_PICKER_RESULT_KEY, null)
					.collect { grade ->
						if (grade == null)
							return@collect

						viewModel.setMaxGradeAction(grade = grade)
						backStackEntry.savedStateHandle[MAX_GRADE_PICKER_RESULT_KEY] = null
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
			val resultHandle = navController.previousBackStackEntry?.savedStateHandle

			GradePickerContentDialog(
				selectedGrade = args.grade,
				maxGrade = args.maxGrade,
				onGradeChange = { grade ->
					resultHandle?.set(GRADE_PICKER_RESULT_KEY, grade)
				},
				onDismissRequest = onDismissRequest
			)
		}

		dialog<EvaluationsDestination.MaxGradePickerDialog> { backStackEntry ->
			val args = backStackEntry.toRoute<EvaluationsDestination.MaxGradePickerDialog>()
			val resultHandle = navController.previousBackStackEntry?.savedStateHandle

			MaxGradePickerContentDialog(
				selectedGrade = args.grade,
				onGradeChange = { grade ->
					resultHandle?.set(MAX_GRADE_PICKER_RESULT_KEY, grade)
				},
				onDismissRequest = onDismissRequest
			)
		}

		dialog<EvaluationsDestination.EvaluationGradePickerDialog> { backStackEntry ->
			val args = backStackEntry.toRoute<EvaluationsDestination.EvaluationGradePickerDialog>()
			val resultHandle = navController.previousBackStackEntry?.savedStateHandle

			EvaluationGradePickerContentDialog(
				selectedGrade = args.grade,
				maxGrade = args.maxGrade,
				onGradeChange = { grade ->
					resultHandle?.set(
						EVALUATION_GRADE_PICKER_RESULT_KEY,
						"${args.evaluationId}|$grade"
					)
				},
				onDismissRequest = onDismissRequest
			)
		}
	}
}
