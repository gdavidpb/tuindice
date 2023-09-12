package com.gdavidpb.tuindice.login.presentation.route

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.login.presentation.contract.SignOut
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignOutViewModel
import com.gdavidpb.tuindice.login.ui.dialog.SignOutDialog
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignOutRoute(
	onNavigateToSignIn: () -> Unit,
	onDismissRequest: () -> Unit,
	viewModel: SignOutViewModel = koinViewModel()
) {
	CollectEffectWithLifecycle(flow = viewModel.viewEvent) { event ->
		when (event) {
			is SignOut.Event.NavigateToSignIn ->
				onNavigateToSignIn()
		}
	}

	SignOutDialog(
		onConfirmClick = viewModel::signOutAction,
		onDismissRequest = onDismissRequest
	)
}