package com.gdavidpb.tuindice.evaluations.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import com.gdavidpb.tuindice.evaluations.ui.view.AddEvaluationContentView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationFailedView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationLoadingView

@Composable
fun AddEvaluationScreen(
	state: AddEvaluation.State,
	onSubjectChange: (subject: Subject) -> Unit,
	onTypeChange: (type: EvaluationType) -> Unit,
	onDateChange: (date: Long?) -> Unit,
	onGradeClick: (grade: Double?, maxGrade: Double?) -> Unit,
	onMaxGradeClick: (grade: Double?) -> Unit,
	onDoneClick: () -> Unit,
	onRetryClick: () -> Unit
) {
	SealedCrossfade(
		targetState = state
	) { targetState ->
		when (targetState) {
			is AddEvaluation.State.Loading ->
				EvaluationLoadingView()

			is AddEvaluation.State.Content ->
				AddEvaluationContentView(
					state = targetState,
					onSubjectChange = onSubjectChange,
					onTypeChange = onTypeChange,
					onDateChange = onDateChange,
					onGradeClick = onGradeClick,
					onMaxGradeClick = onMaxGradeClick,
					onDoneClick = onDoneClick
				)

			is AddEvaluation.State.Failed ->
				EvaluationFailedView(
					onRetryClick = onRetryClick
				)
		}
	}
}