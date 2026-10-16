package com.gdavidpb.tuindice.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
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
	onShowSnackBar: (message: String) -> Unit = {},
	onSessionInvalidated: suspend (message: String?) -> Unit = {},
	onNavigateToSignOutDialog: (pendingChanges: PendingChanges) -> Unit = {},
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

			is Main.Effect.ShowSnackBar ->
				onShowSnackBar(effect.message)

			is Main.Effect.NavigateToSignOutDialog ->
				onNavigateToSignOutDialog(effect.pendingChanges)
		}
	}

	val onSessionInvalidatedState by rememberUpdatedState(onSessionInvalidated)
	val pendingSessionEnd = viewState.pendingSessionEnd

	// The way to the sign-in is state: whoever composes this attends it, and is told to attend it again
	// if it leaves the composition first (a rotation, or the route being taken out while it waits for the
	// navigator). Only when it was attended does it say so, and only then does the machine clear it and
	// take the notice kept for the next start.
	LaunchedEffect(pendingSessionEnd?.id) {
		if (pendingSessionEnd != null) {
			onSessionInvalidatedState(pendingSessionEnd.message)

			viewModel.sessionEndHandledAction(id = pendingSessionEnd.id)
		}
	}

	content(viewState)
}
