package com.gdavidpb.tuindice.auth.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navigation
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectCurrentEntryValueWithLifecycle
import com.gdavidpb.tuindice.auth.presentation.route.SignInRoute
import com.gdavidpb.tuindice.auth.presentation.route.SignOutRoute
import com.gdavidpb.tuindice.auth.presentation.route.UpdatePasswordRoute
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignOutViewModel
import com.gdavidpb.tuindice.auth.presentation.viewmodel.UpdatePasswordViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.authNavigation(
	navController: NavHostController,
	onNavigateToSignIn: () -> Unit,
	onNavigateToSummary: () -> Unit,
	onNavigateToBrowser: (title: String, url: String) -> Unit,
	onDismissRequest: () -> Unit,
	onViewStateChanged: (ViewState) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	dismissSnackBar: () -> Unit = {}
) {
	navigation<AuthDestination.NavGraph>(startDestination = AuthDestination.SignIn) {
		composable<AuthDestination.SignIn> { backStackEntry ->
			val viewModel = koinViewModel<SignInViewModel>(viewModelStoreOwner = backStackEntry)
			val viewState by viewModel.state.collectAsStateWithLifecycle()

			navController.CollectCurrentEntryValueWithLifecycle(
				backStackEntry = backStackEntry,
				value = viewState,
				onValue = onViewStateChanged
			)

			SignInRoute(
				onNavigateToSummary = onNavigateToSummary,
				onNavigateToBrowser = onNavigateToBrowser,
				showSnackBar = showSnackBar,
				dismissSnackBar = dismissSnackBar,
				viewModel = viewModel
			)
		}

		dialog<AuthDestination.SignOutDialog> { backStackEntry ->
			val viewModel = koinViewModel<SignOutViewModel>(viewModelStoreOwner = backStackEntry)

			SignOutRoute(
				onNavigateToSignIn = onNavigateToSignIn,
				onNavigateToUpdatePassword = {
					navController.navigate(AuthDestination.UpdatePasswordDialog)
				},
				onDismissRequest = onDismissRequest,
				showSnackBar = showSnackBar,
				viewModel = viewModel
			)
		}

		dialog<AuthDestination.UpdatePasswordDialog> { backStackEntry ->
			val viewModel = koinViewModel<UpdatePasswordViewModel>(viewModelStoreOwner = backStackEntry)

			UpdatePasswordRoute(
				onDismissRequest = onDismissRequest,
				showSnackBar = showSnackBar,
				viewModel = viewModel
			)
		}
	}
}
