package com.gdavidpb.tuindice.evaluations.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation

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
	onRetryClick: () -> Unit,
	loadingContent: @Composable () -> Unit,
	contentStateContent: @Composable (
		state: Evaluation.State.Content,
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
		) -> Unit
	) -> Unit,
	failedContent: @Composable (onRetryClick: () -> Unit) -> Unit
) {
	SealedCrossfade(
		targetState = state
	) { targetState ->
		when (targetState) {
			is Evaluation.State.Loading ->
				loadingContent()

			is Evaluation.State.Content ->
				contentStateContent(
					targetState,
					onSubjectChange,
					onTypeChange,
					onDateChange,
					onGradeClick,
					onMaxGradeClick,
					onDoneClick
				)

			is Evaluation.State.Failed ->
				failedContent(onRetryClick)
		}
	}
}
