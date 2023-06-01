package com.gdavidpb.tuindice.login.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.login.presentation.viewmodel.UpdatePasswordViewModel
import com.gdavidpb.tuindice.login.ui.dialog.UpdatePasswordDialog
import org.koin.androidx.compose.koinViewModel

@Composable
fun UpdatePasswordRoute(
	onDismissRequest: () -> Unit,
	showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit,
	viewModel: UpdatePasswordViewModel = koinViewModel()
) {
	val viewState by viewModel.viewState.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.viewEvent) { event ->
		when (event) {
			is UpdatePassword.Event.CloseDialog ->
				onDismissRequest()

			is UpdatePassword.Event.ShowSnackBar ->
				showSnackBar(event.message, null, null)
		}
	}

	UpdatePasswordDialog(
		state = viewState,
		onPasswordChanged = { password ->
			val currentState = viewModel.getCurrentState()

			if (currentState is UpdatePassword.State.Idle)
				viewModel.setState(currentState.copy(password = password))
		},
		onConfirmClick = viewModel::signInAction,
		onDismissRequest = onDismissRequest
	)
}