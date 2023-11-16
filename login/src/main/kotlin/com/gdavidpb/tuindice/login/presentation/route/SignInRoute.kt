package com.gdavidpb.tuindice.login.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.login.ui.screen.SignInScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignInRoute(
	onNavigateToSummary: () -> Unit,
	onNavigateToBrowser: (title: String, url: String) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: SignInViewModel = koinViewModel()
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

	SignInScreen(
		state = viewState,
		onUsbIdChange = viewModel::setUsbIdAction,
		onPasswordChange = viewModel::setPasswordAction,
		onSignInClick = viewModel::signInAction,
		onTermsAndConditionsClick = viewModel::openTermsAndConditionsAction,
		onPrivacyPolicyClick = viewModel::openPrivacyPolicyAction
	)
}