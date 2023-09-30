package com.gdavidpb.tuindice.evaluations.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationFailedView
import com.gdavidpb.tuindice.evaluations.ui.view.EvaluationLoadingView

@Composable
fun AddEvaluationScreen(
	state: AddEvaluation.State,
	onRetryClick: () -> Unit
) {
	SealedCrossfade(
		targetState = state
	) { targetState ->
		when (targetState) {
			is AddEvaluation.State.Loading ->
				EvaluationLoadingView()

			is AddEvaluation.State.Step1 -> {
				/* TODO */
			}

			is AddEvaluation.State.Step2 -> {
				/* TODO */
			}

			is AddEvaluation.State.Failed ->
				EvaluationFailedView(
					onRetryClick = onRetryClick
				)
		}
	}
}