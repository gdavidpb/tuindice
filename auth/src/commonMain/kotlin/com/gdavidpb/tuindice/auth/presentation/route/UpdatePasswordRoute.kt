package com.gdavidpb.tuindice.auth.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.auth.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.auth.presentation.viewmodel.UpdatePasswordViewModel
import com.gdavidpb.tuindice.auth.ui.dialog.UpdatePasswordContentDialog

@Composable
fun UpdatePasswordRoute(
	onDismissRequest: () -> Unit,
	onPasswordUpdated: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: UpdatePasswordViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle(
		minActiveState = Lifecycle.State.CREATED
	)

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is UpdatePassword.Effect.PasswordUpdated -> {
				showSnackBar(SnackBarMessage(message = effect.message))
				onPasswordUpdated()
			}

			is UpdatePassword.Effect.ShowSnackBar -> {
				showSnackBar(SnackBarMessage(message = effect.message))
				onDismissRequest()
			}
		}
	}

	UpdatePasswordContentDialog(
		state = viewState,
		onPasswordChange = viewModel::setPasswordAction,
		onPasswordVisibilityToggle = viewModel::togglePasswordVisibilityAction,
		onConfirmClick = viewModel::signInAction,
		onDismissRequest = onDismissRequest
	)
}
