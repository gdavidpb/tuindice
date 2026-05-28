package com.gdavidpb.tuindice.evaluations.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
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
	onAttemptChange: (attempt: EditableAttemptDescriptor?) -> Unit,
	onTypeChange: (type: EvaluationType?) -> Unit,
	onDateChange: (date: Long?) -> Unit,
	onGradeClick: (evaluationName: String, subjectCode: String, grade: Double?, maxGrade: Double?) -> Unit,
	onMaxGradeClick: (evaluationName: String, subjectCode: String, grade: Double?) -> Unit,
	onDoneClick: (
		attempt: EditableAttemptDescriptor?,
		type: EvaluationType?,
		scheduleMode: EvaluationScheduleMode,
		date: Long?,
		grade: Double?,
		maxGrade: Double?
	) -> Unit,
	onRetryClick: () -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
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
						onAttemptChange = onAttemptChange,
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
}
