package com.gdavidpb.tuindice.auth.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignOutViewModel
import com.gdavidpb.tuindice.auth.ui.screen.SignOutScreen

@Composable
fun SignOutRoute(
	initialPendingChanges: PendingChanges,
	onNavigateToSignIn: () -> Unit,
	onNavigateToUpdatePassword: () -> Unit,
	onDismissRequest: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: SignOutViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	val screenState = viewState.withInitialPendingChanges(initialPendingChanges)

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is SignOut.Effect.NavigateToSignIn ->
				onNavigateToSignIn()

			is SignOut.Effect.NavigateToUpdatePassword ->
				onNavigateToUpdatePassword()

			is SignOut.Effect.ShowSnackBar ->
				showSnackBar(
					SnackBarMessage(
						message = effect.message
					)
				)
		}
	}

	LaunchedEffect(initialPendingChanges) {
		viewModel.initializeAction(initialPendingChanges)
	}

	SignOutScreen(
		state = screenState,
		onConfirmClick = {
			viewModel.signOutAction(resolvedPendingChanges = initialPendingChanges)
		},
		onSecondaryClick = viewModel::forceSignOutAction,
		onDismissRequest = onDismissRequest
	)
}

private fun SignOut.State.withInitialPendingChanges(
	pendingChanges: PendingChanges
): SignOut.State {
	return if (this == SignOut.State.Plain && pendingChanges.totalCount > 0) {
		SignOut.State.Pending(pendingChanges)
	} else {
		this
	}
}
