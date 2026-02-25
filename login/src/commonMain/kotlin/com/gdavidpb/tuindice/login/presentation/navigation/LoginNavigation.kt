package com.gdavidpb.tuindice.login.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navigation
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
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
	onViewStateChanged: (ViewState) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	navigation<LoginDestination.NavGraph>(startDestination = LoginDestination.SignIn) {
		composable<LoginDestination.SignIn> {
			val viewModel = koinInject<SignInViewModel>()
			val viewState by viewModel.state.collectAsStateWithLifecycle()

			LaunchedEffect(viewState) {
				onViewStateChanged(viewState)
			}

			SignInRoute(
				onNavigateToSummary = onNavigateToSummary,
				onNavigateToBrowser = onNavigateToBrowser,
				showSnackBar = showSnackBar,
				viewModel = viewModel
			)
		}

		dialog<LoginDestination.SignOutDialog> {
			val viewModel = koinInject<SignOutViewModel>()

			SignOutRoute(
				onNavigateToSignIn = onNavigateToSignIn,
				onDismissRequest = onDismissRequest,
				showSnackBar = showSnackBar,
				viewModel = viewModel
			)
		}

		dialog<LoginDestination.UpdatePasswordDialog> {
			val viewModel = koinInject<UpdatePasswordViewModel>()

			UpdatePasswordRoute(
				onDismissRequest = onDismissRequest,
				showSnackBar = showSnackBar,
				viewModel = viewModel
			)
		}
	}
}
