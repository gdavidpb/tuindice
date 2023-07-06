package com.gdavidpb.tuindice.evaluations.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.GetEvaluationParams
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.ui.screen.EvaluationScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun EvaluationRoute(
	evaluationId: String,
	showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit,
	viewModel: EvaluationViewModel = koinViewModel()
) {
	fun loadEvaluation(evaluationId: String) {
		if (evaluationId.isNotEmpty())
			viewModel.loadEvaluationAction(
				GetEvaluationParams(
					evaluationId = evaluationId
				)
			)
	}

	val viewState by viewModel.viewState.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.viewEvent) { event ->
		when (event) {
			is Evaluation.Event.ShowSnackBar ->
				showSnackBar(
					event.message,
					null,
					null
				)
		}
	}

	LaunchedEffect(Unit) {
		loadEvaluation(evaluationId)
	}

	EvaluationScreen(
		state = viewState,
		onRetryClick = {
			loadEvaluation(evaluationId)
		}
	)
}