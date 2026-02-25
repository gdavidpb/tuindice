package com.gdavidpb.tuindice.login.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.login.presentation.viewmodel.UpdatePasswordViewModel

@Composable
fun UpdatePasswordRoute(
	onDismissRequest: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: UpdatePasswordViewModel,
	content: @Composable (
		state: UpdatePassword.State,
		onPasswordChange: (value: String) -> Unit,
		onConfirmClick: (password: String) -> Unit,
		onDismissRequest: () -> Unit
	) -> Unit
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is UpdatePassword.Effect.ShowSnackBar -> {
				showSnackBar(SnackBarMessage(message = effect.message))
				onDismissRequest()
			}
		}
	}

	content(
		viewState,
		viewModel::setPasswordAction,
		viewModel::signInAction,
		onDismissRequest
	)
}
