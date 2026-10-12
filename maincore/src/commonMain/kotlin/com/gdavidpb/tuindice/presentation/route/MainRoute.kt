package com.gdavidpb.tuindice.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

			is Main.Effect.SessionInvalidated -> {
				// A message means a notice owed that the machine holds a lock for: it is confirmed when it
				// was shown and released when this coroutine ended without showing it (a cancellation while
				// it waited for the navigator is the usual one). Without a message there is no lock to free.
				var settled = effect.message == null

				try {
					onSessionInvalidated(effect.message)

					if (effect.message != null) {
						// Shown: only now is the copy kept for the next start taken. A process that dies
						// before this line (a background kill) leaves it, and the next start explains the
						// sign-in screen.
						viewModel.sessionInvalidationShownAction()
					}

					settled = true
				} finally {
					// sendAction does not suspend, so this is also safe from a cancelled coroutine.
					if (!settled) viewModel.sessionInvalidationNotShownAction()
				}
			}

			is Main.Effect.NavigateToSignOutDialog ->
				onNavigateToSignOutDialog(effect.pendingChanges)
		}
	}

	content(viewState)
}
