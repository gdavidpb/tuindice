package com.gdavidpb.tuindice.auth.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignOutViewModel
import com.gdavidpb.tuindice.auth.ui.screen.SignOutScreen

@Composable
fun SignOutRoute(
	onNavigateToSignIn: () -> Unit,
	onNavigateToUpdatePassword: () -> Unit,
	onDismissRequest: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: SignOutViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

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

	LaunchedEffect(Unit) {
		viewModel.loadPendingChangesAction()
	}

	SignOutScreen(
		state = viewState,
		onConfirmClick = viewModel::signOutAction,
		onSecondaryClick = viewModel::forceSignOutAction,
		onDismissRequest = onDismissRequest
	)
}
