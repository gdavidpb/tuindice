package com.gdavidpb.tuindice.evaluations.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationContentView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationFailedView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationLoadingView

@Composable
fun EvaluationScreen(
	state: Evaluation.State,
	onNameChange: (name: String) -> Unit,
	onSubjectChange: (subject: Subject) -> Unit,
	onDateChange: (date: Long) -> Unit,
	onGradeChange: (grade: Double?) -> Unit,
	onTypeChange: (type: EvaluationType?) -> Unit,
	onDoneClick: () -> Unit,
	onRetryClick: () -> Unit
) {
	SealedCrossfade(
		targetState = state
	) { targetState ->
		when (targetState) {
			is Evaluation.State.Loading ->
				EvaluationLoadingView()

			is Evaluation.State.Content ->
				EvaluationContentView(
					state = targetState,
					onNameChange = onNameChange,
					onSubjectChange = onSubjectChange,
					onDateChange = onDateChange,
					onGradeChange = onGradeChange,
					onTypeChange = onTypeChange,
					onDoneClick = onDoneClick
				)

			is Evaluation.State.Failed ->
				EvaluationFailedView(
					onRetryClick = onRetryClick
				)
		}
	}
}