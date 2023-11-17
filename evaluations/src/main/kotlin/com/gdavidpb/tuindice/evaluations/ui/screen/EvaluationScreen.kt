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
					onSubjectChange = onSubjectChange,
					onTypeChange = onTypeChange,
					onDateChange = onDateChange,
					onGradeClick = onGradeClick,
					onMaxGradeClick = onMaxGradeClick,
					onDoneClick = onDoneClick
				)

			is Evaluation.State.Failed ->
				EvaluationFailedView(
					onRetryClick = onRetryClick
				)
		}
	}
}