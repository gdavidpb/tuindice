package com.gdavidpb.tuindice.login.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.login.presentation.viewmodel.UpdatePasswordViewModel
import com.gdavidpb.tuindice.login.ui.dialog.UpdatePasswordDialog
import org.koin.androidx.compose.koinViewModel

@Composable
fun UpdatePasswordRoute(
	onDismissRequest: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: UpdatePasswordViewModel = koinViewModel()
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is UpdatePassword.Effect.CloseDialog ->
				onDismissRequest()

			is UpdatePassword.Effect.ShowSnackBar ->
				showSnackBar(SnackBarMessage(message = effect.message))
		}
	}

	UpdatePasswordDialog(
		state = viewState,
		onPasswordChange = viewModel::setPasswordAction,
		onConfirmClick = viewModel::signInAction,
		onDismissRequest = onDismissRequest
	)
}