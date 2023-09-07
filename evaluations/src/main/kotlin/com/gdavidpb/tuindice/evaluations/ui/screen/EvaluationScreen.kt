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
	onNameChanged: (name: String) -> Unit,
	onSubjectChanged: (subject: Subject) -> Unit,
	onDateChanged: (date: Long) -> Unit,
	onGradeChanged: (grade: Double?) -> Unit,
	onTypeChanged: (type: EvaluationType?) -> Unit,
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
					onNameChanged = onNameChanged,
					onSubjectChanged = onSubjectChanged,
					onDateChanged = onDateChanged,
					onGradeChanged = onGradeChanged,
					onTypeChanged = onTypeChanged,
					onDoneClick = onDoneClick
				)

			is Evaluation.State.Failed ->
				EvaluationFailedView(
					onRetryClick = onRetryClick
				)
		}
	}
}