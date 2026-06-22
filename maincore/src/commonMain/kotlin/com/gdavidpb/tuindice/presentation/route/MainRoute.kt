package com.gdavidpb.tuindice.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.model.UpdateLaunchResult
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel

@Composable
fun MainRoute(
	onNavigateToGooglePlayServicesUnavailableDialog: () -> Unit,
	onRequestReviewFlow: suspend () -> Unit,
	onRequestUpdateFlow: suspend (UpdateAction) -> UpdateLaunchResult,
	onOpenUpdateStoreFallback: suspend (UpdateLaunchResult.OpenStoreFallback) -> Unit = {},
	viewModel: MainViewModel,
	content: @Composable (state: Main.State) -> Unit
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Main.Effect.TriggerUpdateFlow -> {
				val result = onRequestUpdateFlow(effect.action)
				viewModel.updateFlowCompletedAction(result = result)
			}

			is Main.Effect.OpenUpdateStoreFallback ->
				onOpenUpdateStoreFallback(effect.result)

			is Main.Effect.NavigateToGooglePlayServicesUnavailableDialog ->
				onNavigateToGooglePlayServicesUnavailableDialog()

			is Main.Effect.TriggerReviewFlow ->
				onRequestReviewFlow()
		}
	}

	content(viewState)
}
