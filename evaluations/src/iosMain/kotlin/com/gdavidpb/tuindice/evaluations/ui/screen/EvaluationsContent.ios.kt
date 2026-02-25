package com.gdavidpb.tuindice.evaluations.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.ui.view.EmptyStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationItemList
import com.gdavidpb.tuindice.evaluations.ui.dialog.GradePickerDialog
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationContentView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationFailedView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationLoadingView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationsContentView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationsEmptyView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationsFailedView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationsLoadingView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationsNoSubjectsView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationsView
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.EvaluationGradeWheelPickerDefaults
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.*

@Composable
fun EvaluationsContentScreen(
	state: Evaluations.State,
	onAddEvaluationClick: () -> Unit,
	onEvaluationClick: (evaluationId: String) -> Unit,
	onEvaluationEdit: (evaluationId: String) -> Unit,
	onEvaluationDelete: (evaluationId: String) -> Unit,
	onFilterCheckedChange: (filter: EvaluationFilter, isChecked: Boolean) -> Unit,
	onClearFiltersClick: () -> Unit,
	onRetryClick: () -> Unit
) {
	EvaluationsScreen(
		state = state,
		onAddEvaluationClick = onAddEvaluationClick,
		onEvaluationClick = onEvaluationClick,
		onEvaluationEdit = onEvaluationEdit,
		onEvaluationDelete = onEvaluationDelete,
		onFilterCheckedChange = onFilterCheckedChange,
		onClearFiltersClick = onClearFiltersClick,
		onRetryClick = onRetryClick,
		loadingContent = {
			EvaluationsLoadingView()
		},
		contentStateContent = { contentState, addClick, evaluationClick, evaluationEdit, evaluationDelete, filterChange, clearClick ->
			val evaluations = contentState
				.filteredEvaluations
				.toEvaluationItemList()

			EvaluationsContentView(
				state = contentState,
				hasEvaluations = evaluations.isNotEmpty(),
				emptyMatchTitle = stringResource(Res.string.title_empty_match_evaluations),
				emptyMatchMessage = stringResource(Res.string.message_empty_match_evaluations),
				onAddEvaluationClick = addClick,
				onClearFiltersClick = clearClick,
				onFilterCheckedChange = filterChange,
				onEvaluationClick = evaluationClick,
				onEvaluationEdit = evaluationEdit,
				onEvaluationDelete = evaluationDelete,
				addFabContent = {
					Icon(
						imageVector = Icons.Outlined.Add,
						contentDescription = null
					)
				},
				clearFiltersFabContent = {
					Icon(
						imageVector = Icons.Outlined.FilterAltOff,
						contentDescription = null
					)
				},
				emptyMatchHeaderContent = {
					EmptyStateAnimationView()
				},
				evaluationsContent = { lazyListState ->
					EvaluationsView(
						lazyListState = lazyListState,
						evaluations = evaluations,
						onEvaluationClick = evaluationClick,
						onEvaluationEdit = evaluationEdit,
						onEvaluationDelete = evaluationDelete
					) { _, _, _, content -> content() }
				}
			)
		},
		failedContent = { retry ->
			EvaluationsFailedView(
				title = stringResource(Res.string.view_error_title),
				message = stringResource(Res.string.view_error_message),
				retryText = stringResource(Res.string.view_error_retry),
				onRetryClick = retry,
				headerContent = {
					ErrorStateAnimationView()
				}
			)
		},
		noSubjectsContent = {
			EvaluationsNoSubjectsView(
				title = stringResource(Res.string.title_no_subjects_evaluations),
				message = stringResource(Res.string.message_no_subjects_evaluations),
				headerContent = {
					EmptyStateAnimationView()
				}
			)
		},
		emptyContent = { addClick ->
			EvaluationsEmptyView(
				title = stringResource(Res.string.title_empty_evaluations),
				message = stringResource(Res.string.message_empty_evaluations),
				actionLabel = stringResource(Res.string.button_add_evaluation),
				onAddEvaluationClick = addClick,
				headerContent = {
					EmptyStateAnimationView()
				}
			)
		}
	)
}

@Composable
fun EvaluationContentScreen(
	state: Evaluation.State,
	onSubjectChange: (subject: Subject) -> Unit,
	onTypeChange: (type: EvaluationType) -> Unit,
	onDateChange: (date: Long?) -> Unit,
	onGradeClick: (grade: Double?, maxGrade: Double?) -> Unit,
	onMaxGradeClick: (grade: Double?) -> Unit,
	onDoneClick: (subject: Subject?, type: EvaluationType?, date: Long?, grade: Double?, maxGrade: Double?) -> Unit,
	onRetryClick: () -> Unit
) {
	EvaluationScreen(
		state = state,
		onSubjectChange = onSubjectChange,
		onTypeChange = onTypeChange,
		onDateChange = onDateChange,
		onGradeClick = onGradeClick,
		onMaxGradeClick = onMaxGradeClick,
		onDoneClick = onDoneClick,
		onRetryClick = onRetryClick,
		loadingContent = {
			EvaluationLoadingView()
		},
		contentStateContent = { contentState, subjectChange, typeChange, dateChange, gradeClick, maxGradeClick, doneClick ->
			EvaluationContentView(
				state = contentState,
				onSubjectChange = subjectChange,
				onTypeChange = typeChange,
				onDateChange = dateChange,
				onGradeClick = gradeClick,
				onMaxGradeClick = maxGradeClick,
				onDoneClick = doneClick
			)
		},
		failedContent = { retry ->
			EvaluationFailedView(
				title = stringResource(Res.string.view_error_title),
				message = stringResource(Res.string.view_error_message),
				retryText = stringResource(Res.string.view_error_retry),
				onRetryClick = retry,
				headerContent = {
					ErrorStateAnimationView()
				}
			)
		}
	)
}

@Composable
fun GradePickerContentDialog(
	selectedGrade: Double?,
	maxGrade: Double?,
	onGradeChange: (grade: Double) -> Unit,
	onDismissRequest: () -> Unit
) {
	GradePickerDialog(
		title = stringResource(Res.string.dialog_title_add_evaluation_grade),
		acceptText = stringResource(Res.string.accept),
		cancelText = stringResource(Res.string.cancel),
		selectedGrade = selectedGrade ?: maxGrade ?: 0.0,
		gradeRange = 0.0..(maxGrade ?: 100.0),
		onGradeChange = onGradeChange,
		onDismissRequest = onDismissRequest
	)
}

@Composable
fun MaxGradePickerContentDialog(
	selectedGrade: Double?,
	onGradeChange: (grade: Double) -> Unit,
	onDismissRequest: () -> Unit
) {
	GradePickerDialog(
		title = stringResource(Res.string.dialog_title_add_evaluation_max_grade),
		acceptText = stringResource(Res.string.accept),
		cancelText = stringResource(Res.string.cancel),
		selectedGrade = selectedGrade ?: 100.0,
		gradeRange = EvaluationGradeWheelPickerDefaults.GradeRange,
		onGradeChange = onGradeChange,
		onDismissRequest = onDismissRequest
	)
}

@Composable
fun EvaluationGradePickerContentDialog(
	selectedGrade: Double?,
	maxGrade: Double,
	onGradeChange: (grade: Double) -> Unit,
	onDismissRequest: () -> Unit
) {
	GradePickerDialog(
		title = stringResource(Res.string.dialog_title_edit_evaluation_grade),
		acceptText = stringResource(Res.string.accept),
		cancelText = stringResource(Res.string.cancel),
		selectedGrade = selectedGrade ?: maxGrade,
		gradeRange = 0.0..maxGrade,
		onGradeChange = onGradeChange,
		onDismissRequest = onDismissRequest
	)
}
