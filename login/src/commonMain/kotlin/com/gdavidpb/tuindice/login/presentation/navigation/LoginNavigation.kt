package com.gdavidpb.tuindice.login.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navigation
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.presentation.contract.SignOut
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.login.presentation.route.SignInRoute
import com.gdavidpb.tuindice.login.presentation.route.SignOutRoute
import com.gdavidpb.tuindice.login.presentation.route.UpdatePasswordRoute
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignOutViewModel
import com.gdavidpb.tuindice.login.presentation.viewmodel.UpdatePasswordViewModel
import org.koin.compose.koinInject

fun NavGraphBuilder.loginNavigation(
	onNavigateToSignIn: () -> Unit,
	onNavigateToSummary: () -> Unit,
	onNavigateToBrowser: (title: String, url: String) -> Unit,
	onDismissRequest: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	signInContent: @Composable (
		state: SignIn.State,
		onUsbIdChange: (String) -> Unit,
		onPasswordChange: (String) -> Unit,
		onSignInClick: (usbId: String, password: String) -> Unit,
		onTermsAndConditionsClick: () -> Unit,
		onPrivacyPolicyClick: () -> Unit
	) -> Unit,
	signOutDialogContent: @Composable (
		state: SignOut.State,
		onConfirmClick: () -> Unit,
		onDismissRequest: () -> Unit
	) -> Unit,
	updatePasswordDialogContent: @Composable (
		state: UpdatePassword.State,
		onPasswordChange: (String) -> Unit,
		onConfirmClick: (password: String) -> Unit,
		onDismissRequest: () -> Unit
	) -> Unit
) {
	navigation<LoginDestination.NavGraph>(startDestination = LoginDestination.SignIn) {
		composable<LoginDestination.SignIn> {
			val viewModel = koinInject<SignInViewModel>()

			SignInRoute(
				onNavigateToSummary = onNavigateToSummary,
				onNavigateToBrowser = onNavigateToBrowser,
				showSnackBar = showSnackBar,
				viewModel = viewModel,
				content = signInContent
			)
		}

		dialog<LoginDestination.SignOutDialog> {
			val viewModel = koinInject<SignOutViewModel>()

			SignOutRoute(
				onNavigateToSignIn = onNavigateToSignIn,
				onDismissRequest = onDismissRequest,
				showSnackBar = showSnackBar,
				viewModel = viewModel,
				content = signOutDialogContent
			)
		}

		dialog<LoginDestination.UpdatePasswordDialog> {
			val viewModel = koinInject<UpdatePasswordViewModel>()

			UpdatePasswordRoute(
				onDismissRequest = onDismissRequest,
				showSnackBar = showSnackBar,
				viewModel = viewModel,
				content = updatePasswordDialogContent
			)
		}
	}
}
