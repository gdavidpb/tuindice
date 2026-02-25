package com.gdavidpb.tuindice.login.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignInViewModel

@Composable
fun SignInRoute(
	onNavigateToSummary: () -> Unit,
	onNavigateToBrowser: (title: String, url: String) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: SignInViewModel,
	content: @Composable (
		state: SignIn.State,
		onUsbIdChange: (value: String) -> Unit,
		onPasswordChange: (value: String) -> Unit,
		onSignInClick: (usbId: String, password: String) -> Unit,
		onTermsAndConditionsClick: () -> Unit,
		onPrivacyPolicyClick: () -> Unit
	) -> Unit
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is SignIn.Effect.NavigateToSummary ->
				onNavigateToSummary()

			is SignIn.Effect.NavigateToBrowser ->
				onNavigateToBrowser(
					effect.title,
					effect.url
				)

			is SignIn.Effect.ShowSnackBar ->
				showSnackBar(
					SnackBarMessage(
						message = effect.message
					)
				)

			is SignIn.Effect.ShowRetrySnackBar ->
				showSnackBar(
					SnackBarMessage(
						message = effect.message,
						actionLabel = effect.actionLabel,
						onAction = {
							viewModel.signInAction(
								usbId = effect.params.usbId,
								password = effect.params.password
							)
						}
					)
				)
		}
	}

	content(
		viewState,
		viewModel::setUsbIdAction,
		viewModel::setPasswordAction,
		viewModel::signInAction,
		viewModel::openTermsAndConditionsAction,
		viewModel::openPrivacyPolicyAction
	)
}
