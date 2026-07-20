package com.gdavidpb.tuindice.evaluations.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.base.presentation.navigation.NavShellBindings
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavActions
import com.gdavidpb.tuindice.base.presentation.navigation.dialogMetadata
import com.gdavidpb.tuindice.base.utils.extension.CollectCurrentEntryValueWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectNavResultWithLifecycle
import com.gdavidpb.tuindice.evaluations.presentation.route.EvaluationRoute
import com.gdavidpb.tuindice.evaluations.presentation.route.EvaluationsRoute
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.evaluations.ui.dialog.DeleteEvaluationConfirmationContentDialog
import com.gdavidpb.tuindice.evaluations.ui.dialog.EvaluationGradePickerContentDialog
import com.gdavidpb.tuindice.evaluations.ui.dialog.GradePickerContentDialog
import com.gdavidpb.tuindice.evaluations.ui.dialog.MaxGradePickerContentDialog
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.evaluationsEntries(
	navActions: TuIndiceNavActions,
	shellBindings: NavShellBindings
) {
	evaluationsEntry(navActions = navActions, shellBindings = shellBindings)
	evaluationEntry(navActions = navActions, shellBindings = shellBindings)
	gradePickerDialogEntries(navActions = navActions)
	evaluationsResultDialogEntries(navActions = navActions)
}

private fun EntryProviderScope<NavKey>.evaluationsEntry(
	navActions: TuIndiceNavActions,
	shellBindings: NavShellBindings
) {
	entry<EvaluationsDestination.Evaluations> {
		val viewModel = koinViewModel<EvaluationsViewModel>()
		val viewState by viewModel.state.collectAsStateWithLifecycle()

		CollectCurrentEntryValueWithLifecycle(
			value = viewState,
			onValue = shellBindings.onViewStateChanged
		)

		CollectNavResultWithLifecycle<EvaluationsBackResult> { result ->
			when (result) {
				is EvaluationsBackResult.SetEvaluationGrade ->
					viewModel.setEvaluationGradeAction(
						evaluationId = result.evaluationId,
						grade = result.grade
					)

				is EvaluationsBackResult.RemoveEvaluation ->
					viewModel.removeEvaluationAction(result.evaluationId)
			}
		}

		EvaluationsRoute(
			onNavigateToAddEvaluation = {
				navActions.push(EvaluationsDestination.Evaluation(evaluationId = null))
			},
			onNavigateToEvaluation = { evaluationId ->
				navActions.push(EvaluationsDestination.Evaluation(evaluationId = evaluationId))
			},
			onNavigateToEvaluationGradePickerDialog = { evaluationId, evaluationName, subjectCode, grade, maxGrade ->
				navActions.push(
					EvaluationsDestination.EvaluationGradePickerDialog(
						evaluationId = evaluationId,
						evaluationName = evaluationName,
						subjectCode = subjectCode,
						grade = grade,
						maxGrade = maxGrade
					)
				)
			},
			onNavigateToDeleteEvaluationConfirmation = { evaluationId ->
				navActions.push(
					EvaluationsDestination.DeleteEvaluationConfirmationDialog(
						evaluationId = evaluationId
					)
				)
			},
			showSnackBar = shellBindings.showSnackBar,
			viewModel = viewModel
		)
	}
}

private fun EntryProviderScope<NavKey>.evaluationEntry(
	navActions: TuIndiceNavActions,
	shellBindings: NavShellBindings
) {
	entry<EvaluationsDestination.Evaluation> { key ->
		val viewModel = koinViewModel<EvaluationViewModel>()
		val viewState by viewModel.state.collectAsStateWithLifecycle()

		CollectCurrentEntryValueWithLifecycle(
			value = viewState,
			onValue = shellBindings.onViewStateChanged
		)

		CollectNavResultWithLifecycle<EvaluationBackResult> { result ->
			when (result) {
				is EvaluationBackResult.SetGrade ->
					viewModel.setGradeAction(result.grade)

				is EvaluationBackResult.SetMaxGrade ->
					viewModel.setMaxGradeAction(result.grade)
			}
		}

		EvaluationRoute(
			evaluationId = key.evaluationId,
			/* Nav2 pushed a second Evaluations list here; the editor is always
			   pushed from the list, so returning is a plain pop (duplicate keys
			   in one stack would also break tab-scoped retention). */
			onNavigateToEvaluations = { navActions.pop() },
			onNavigateToGradePickerDialog = { evaluationName, subjectCode, grade, maxGrade ->
				navActions.push(
					EvaluationsDestination.GradePickerDialog(
						evaluationName = evaluationName,
						subjectCode = subjectCode,
						grade = grade,
						maxGrade = maxGrade
					)
				)
			},
			onNavigateToMaxGradePickerDialog = { evaluationName, subjectCode, maxGrade ->
				navActions.push(
					EvaluationsDestination.MaxGradePickerDialog(
						evaluationName = evaluationName,
						subjectCode = subjectCode,
						grade = maxGrade
					)
				)
			},
			onBack = { navActions.pop() },
			onBackInterceptorAvailable = shellBindings.onBackInterceptorAvailable,
			showSnackBar = shellBindings.showSnackBar,
			viewModel = viewModel
		)
	}
}

private fun EntryProviderScope<NavKey>.gradePickerDialogEntries(
	navActions: TuIndiceNavActions
) {
	entry<EvaluationsDestination.GradePickerDialog>(metadata = dialogMetadata()) { key ->
		GradePickerContentDialog(
			evaluationName = key.evaluationName,
			subjectCode = key.subjectCode,
			selectedGrade = key.grade,
			maxGrade = key.maxGrade,
			onGradeChange = { grade ->
				navActions.popWithResult(EvaluationBackResult.SetGrade(grade))
			},
			onDismissRequest = { navActions.pop() },
			dismissOnConfirm = false
		)
	}

	entry<EvaluationsDestination.MaxGradePickerDialog>(metadata = dialogMetadata()) { key ->
		MaxGradePickerContentDialog(
			evaluationName = key.evaluationName,
			subjectCode = key.subjectCode,
			selectedGrade = key.grade,
			onGradeChange = { grade ->
				navActions.popWithResult(EvaluationBackResult.SetMaxGrade(grade))
			},
			onDismissRequest = { navActions.pop() },
			dismissOnConfirm = false
		)
	}
}

private fun EntryProviderScope<NavKey>.evaluationsResultDialogEntries(
	navActions: TuIndiceNavActions
) {
	entry<EvaluationsDestination.DeleteEvaluationConfirmationDialog>(metadata = dialogMetadata()) { key ->
		DeleteEvaluationConfirmationContentDialog(
			onConfirmClick = {
				navActions.popWithResult(
					EvaluationsBackResult.RemoveEvaluation(evaluationId = key.evaluationId)
				)
			},
			onDismissRequest = { navActions.pop() }
		)
	}

	entry<EvaluationsDestination.EvaluationGradePickerDialog>(metadata = dialogMetadata()) { key ->
		EvaluationGradePickerContentDialog(
			evaluationName = key.evaluationName,
			subjectCode = key.subjectCode,
			selectedGrade = key.grade,
			maxGrade = key.maxGrade,
			onGradeChange = { grade ->
				navActions.popWithResult(
					EvaluationsBackResult.SetEvaluationGrade(
						evaluationId = key.evaluationId,
						grade = grade
					)
				)
			},
			onDismissRequest = { navActions.pop() },
			dismissOnConfirm = false
		)
	}
}
