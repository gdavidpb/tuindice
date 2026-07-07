package com.gdavidpb.tuindice.auth.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.auth.ui.screen.SignInScreen

@Composable
fun SignInRoute(
	onNavigateToSummary: () -> Unit,
	onNavigateToBrowser: (title: String, url: String) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	dismissSnackBar: () -> Unit = {},
	onOutdatedAppDetected: () -> Unit = {},
	viewModel: SignInViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle(
		minActiveState = Lifecycle.State.CREATED
	)

	val onSignInClick: () -> Unit = {
		dismissSnackBar()
		viewModel.signInAction()
	}

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
							// The failed transition restored the credentials into Idle,
							// so retrying is just clicking sign-in again.
							onSignInClick()
						}
					)
				)

				is SignIn.Effect.ShowOutdatedApp ->
					onOutdatedAppDetected()
		}
	}

	SignInScreen(
		state = viewState,
		onUsbIdChange = viewModel::setUsbIdAction,
		onPasswordChange = viewModel::setPasswordAction,
		onPasswordVisibilityToggle = viewModel::togglePasswordVisibilityAction,
		onIdentifierModeToggle = viewModel::toggleIdentifierModeAction,
		onUsageDataCollectionEnabledChange = viewModel::setUsageDataCollectionEnabledAction,
		onSignInClick = onSignInClick,
		onCancelSignInClick = viewModel::cancelSignInAction,
		onTermsAndConditionsClick = viewModel::openTermsAndConditionsAction,
		onPrivacyPolicyClick = viewModel::openPrivacyPolicyAction
	)
}
