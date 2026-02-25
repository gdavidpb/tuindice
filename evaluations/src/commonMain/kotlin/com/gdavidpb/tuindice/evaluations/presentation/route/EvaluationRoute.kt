package com.gdavidpb.tuindice.evaluations.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel

@Composable
fun EvaluationRoute(
	evaluationId: String?,
	onNavigateToEvaluations: () -> Unit,
	onNavigateToGradePickerDialog: (grade: Double?, maxGrade: Double?) -> Unit,
	onNavigateToMaxGradePickerDialog: (maxGrade: Double?) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: EvaluationViewModel,
	content: @Composable (
		state: Evaluation.State,
		onSubjectChange: (subject: Subject) -> Unit,
		onTypeChange: (type: EvaluationType) -> Unit,
		onDateChange: (date: Long?) -> Unit,
		onGradeClick: (grade: Double?, maxGrade: Double?) -> Unit,
		onMaxGradeClick: (grade: Double?) -> Unit,
		onDoneClick: (
			subject: Subject?,
			type: EvaluationType?,
			date: Long?,
			grade: Double?,
			maxGrade: Double?
		) -> Unit,
		onRetryClick: () -> Unit
	) -> Unit
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Evaluation.Effect.NavigateToEvaluations ->
				onNavigateToEvaluations()

			is Evaluation.Effect.NavigateToGradePickerDialog ->
				onNavigateToGradePickerDialog(
					effect.grade,
					effect.maxGrade
				)

			is Evaluation.Effect.NavigateToMaxGradePickerDialog ->
				onNavigateToMaxGradePickerDialog(
					effect.maxGrade
				)

			is Evaluation.Effect.ShowSnackBar ->
				showSnackBar(SnackBarMessage(message = effect.message))
		}
	}

	LaunchedEffect(Unit) {
		if (evaluationId == null)
			viewModel.loadAvailableSubjectsAction()
		else
			viewModel.loadEvaluationAction(evaluationId)
	}

	content(
		viewState,
		viewModel::setSubjectAction,
		viewModel::setTypeAction,
		viewModel::setDateAction,
		viewModel::clickGradeAction,
		viewModel::clickMaxGradeAction,
		{ subject, type, date, grade, maxGrade ->
			if (evaluationId == null)
				viewModel.clickAddEvaluationAction(
					subject = subject,
					type = type,
					date = date,
					grade = grade,
					maxGrade = maxGrade
				)
			else
				viewModel.clickEditEvaluationAction(
					evaluationId = evaluationId,
					subject = subject,
					type = type,
					date = date,
					grade = grade,
					maxGrade = maxGrade
				)
		},
		{
			if (evaluationId == null)
				viewModel.loadAvailableSubjectsAction()
			else
				viewModel.loadEvaluationAction(evaluationId)
		}
	)
}
