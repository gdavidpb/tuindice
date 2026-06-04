package com.gdavidpb.tuindice.evaluations.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.base.ui.view.EmptyStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationsContentView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationsEmptyView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationsFailedView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationsLoadingView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationsNoAttemptsView
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.button_add_evaluation
import tuindice.evaluations.generated.resources.message_empty_evaluations
import tuindice.evaluations.generated.resources.message_no_subjects_evaluations
import tuindice.evaluations.generated.resources.title_empty_evaluations
import tuindice.evaluations.generated.resources.title_no_subjects_evaluations
import tuindice.evaluations.generated.resources.view_error_message
import tuindice.evaluations.generated.resources.view_error_retry
import tuindice.evaluations.generated.resources.view_error_title

@Composable
fun EvaluationsScreen(
	state: Evaluations.State,
	onAddEvaluationClick: () -> Unit,
	onEvaluationClick: (evaluationId: String, evaluationName: String, subjectCode: String) -> Unit,
	onEvaluationEdit: (evaluationId: String) -> Unit,
	onEvaluationDelete: (evaluationId: String) -> Unit,
	onWeekClick: (Int) -> Unit = {},
	onRetryClick: () -> Unit,
	scrollEnabled: Boolean = true,
	openActionsEvaluationId: String? = null,
	focusEvaluationId: String? = null,
	onFocusEvaluationBoundsChange: (Rect?) -> Unit = {}
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
				is Evaluations.State.Idle -> Unit

				is Evaluations.State.Loading ->
					EvaluationsLoadingView()

				is Evaluations.State.Content ->
					EvaluationsContentView(
						state = targetState,
						onAddEvaluationClick = onAddEvaluationClick,
						onWeekClick = onWeekClick,
						onEvaluationClick = onEvaluationClick,
						onEvaluationEdit = onEvaluationEdit,
						onEvaluationDelete = onEvaluationDelete,
						scrollEnabled = scrollEnabled,
						openActionsEvaluationId = openActionsEvaluationId,
						focusEvaluationId = focusEvaluationId,
						onFocusEvaluationBoundsChange = onFocusEvaluationBoundsChange
					)

				is Evaluations.State.Failed ->
					EvaluationsFailedView(
						title = stringResource(Res.string.view_error_title),
						message = stringResource(Res.string.view_error_message),
						retryText = stringResource(Res.string.view_error_retry),
						onRetryClick = onRetryClick,
						headerContent = {
							ErrorStateAnimationView()
						}
					)

				is Evaluations.State.NoAttempts ->
					EvaluationsNoAttemptsView(
						title = stringResource(Res.string.title_no_subjects_evaluations),
						message = stringResource(Res.string.message_no_subjects_evaluations),
						headerContent = {
							EmptyStateAnimationView()
						}
					)

				is Evaluations.State.Empty ->
					EvaluationsEmptyView(
						title = stringResource(Res.string.title_empty_evaluations),
						message = stringResource(Res.string.message_empty_evaluations),
						actionLabel = stringResource(Res.string.button_add_evaluation),
						onAddEvaluationClick = onAddEvaluationClick,
						headerContent = {
							EmptyStateAnimationView()
						}
					)
			}
		}
	}
}
