package com.gdavidpb.tuindice.evaluations.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationContentView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationFailedView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationLoadingView
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.view_error_message
import tuindice.evaluations.generated.resources.view_error_retry
import tuindice.evaluations.generated.resources.view_error_title

@Composable
fun EvaluationScreen(
	state: Evaluation.State,
	onSubjectChange: (subject: Subject?) -> Unit,
	onTypeChange: (type: EvaluationType?) -> Unit,
	onDateChange: (date: Long?) -> Unit,
	onGradeClick: (grade: Double?, maxGrade: Double?) -> Unit,
	onMaxGradeClick: (grade: Double?) -> Unit,
	onDoneClick: (
		subject: Subject?,
		type: EvaluationType?,
		scheduleMode: EvaluationScheduleMode,
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
					title = stringResource(Res.string.view_error_title),
					message = stringResource(Res.string.view_error_message),
					retryText = stringResource(Res.string.view_error_retry),
					onRetryClick = onRetryClick,
					headerContent = {
						ErrorStateAnimationView()
					}
				)
		}
	}
}
