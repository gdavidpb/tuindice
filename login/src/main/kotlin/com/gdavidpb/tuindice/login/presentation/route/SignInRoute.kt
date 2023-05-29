package com.gdavidpb.tuindice.login.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.login.ui.screen.SignInScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignInRoute(
	onNavigateToSummary: () -> Unit,
	onNavigateToBrowser: (title: String, url: String) -> Unit,
	showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit,
	viewModel: SignInViewModel = koinViewModel()
) {
	val viewState by viewModel.viewState.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.viewEvent) { event ->
		when (event) {
			is SignIn.Event.NavigateToSummary ->
				onNavigateToSummary()

			is SignIn.Event.NavigateToBrowser ->
				onNavigateToBrowser(event.title, event.url)

			is SignIn.Event.ShowSnackBar ->
				showSnackBar(
					event.message,
					event.actionLabel,
					viewModel::signInAction
				)
		}
	}

	SignInScreen(
		state = viewState,
		onUsbIdChanged = { usbId ->
			val currentState = viewModel.getCurrentState()

			if (currentState is SignIn.State.Idle)
				viewModel.setState(currentState.copy(usbId = usbId))
		},
		onPasswordChanged = { password ->
			val currentState = viewModel.getCurrentState()

			if (currentState is SignIn.State.Idle)
				viewModel.setState(currentState.copy(password = password))
		},
		onSignInClick = viewModel::signInAction,
		onTermsAndConditionsClick = viewModel::openTermsAndConditionsAction,
		onPrivacyPolicyClick = viewModel::openPrivacyPolicyAction
	)
}