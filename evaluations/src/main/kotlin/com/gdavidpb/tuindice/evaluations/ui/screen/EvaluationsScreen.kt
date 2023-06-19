package com.gdavidpb.tuindice.evaluations.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationsContentView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationsEmptyView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationsFailedView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationsLoadingView

@Composable
fun EvaluationsScreen(
	state: Evaluations.State,
	onAddEvaluationClick: () -> Unit,
	onRetryClick: () -> Unit
) {
	SealedCrossfade(
		targetState = state
	) { targetState ->
		when (targetState) {
			is Evaluations.State.Loading ->
				EvaluationsLoadingView()

			is Evaluations.State.Content ->
				EvaluationsContentView(
					state = targetState,
					onAddEvaluationClick = onAddEvaluationClick
				)

			is Evaluations.State.Failed ->
				EvaluationsFailedView(
					onRetryClick = onRetryClick
				)

			is Evaluations.State.Empty ->
				EvaluationsEmptyView()
		}
	}
}