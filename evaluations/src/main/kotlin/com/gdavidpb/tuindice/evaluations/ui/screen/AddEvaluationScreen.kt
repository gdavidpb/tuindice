package com.gdavidpb.tuindice.evaluations.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import com.gdavidpb.tuindice.evaluations.ui.view.AddEvaluationStep1View
import com.gdavidpb.tuindice.evaluations.ui.view.AddEvaluationStep2View
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationFailedView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationLoadingView

@Composable
fun AddEvaluationScreen(
	state: AddEvaluation.State,
	onSubjectChange: (subject: Subject) -> Unit,
	onTypeChange: (type: EvaluationType) -> Unit,
	onDateChange: (date: Long?) -> Unit,
	onMaxGradeChange: (grade: Double?) -> Unit,
	onNextStepClick: () -> Unit,
	onRetryClick: () -> Unit
) {
	SealedCrossfade(
		targetState = state
	) { targetState ->
		when (targetState) {
			is AddEvaluation.State.Loading ->
				EvaluationLoadingView()

			is AddEvaluation.State.Step1 ->
				AddEvaluationStep1View(
					state = targetState,
					onSubjectChange = onSubjectChange,
					onTypeChange = onTypeChange,
					onNextStepClick = onNextStepClick
				)

			is AddEvaluation.State.Step2 ->
				AddEvaluationStep2View(
					state = targetState,
					onDateChange = onDateChange,
					onMaxGradeChange = onMaxGradeChange,
					onNextStepClick = onNextStepClick
				)

			is AddEvaluation.State.Failed ->
				EvaluationFailedView(
					onRetryClick = onRetryClick
				)
		}
	}
}