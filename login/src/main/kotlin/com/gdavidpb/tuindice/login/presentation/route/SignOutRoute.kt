package com.gdavidpb.tuindice.login.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.login.presentation.contract.SignOut
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignOutViewModel
import com.gdavidpb.tuindice.login.ui.dialog.SignOutDialog
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignOutRoute(
	onNavigateToSignIn: () -> Unit,
	onDismissRequest: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: SignOutViewModel = koinViewModel()
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is SignOut.Effect.NavigateToSignIn ->
				onNavigateToSignIn()

			is SignOut.Effect.ShowSnackBar ->
				showSnackBar(
					SnackBarMessage(
						message = effect.message
					)
				)
		}
	}

	SignOutDialog(
		state = viewState,
		onConfirmClick = viewModel::signOutAction,
		onDismissRequest = onDismissRequest
	)
}