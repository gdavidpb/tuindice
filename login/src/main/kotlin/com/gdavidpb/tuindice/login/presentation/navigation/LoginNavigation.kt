package com.gdavidpb.tuindice.login.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navigation
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.login.presentation.route.SignInRoute
import com.gdavidpb.tuindice.login.presentation.route.SignOutRoute
import com.gdavidpb.tuindice.login.presentation.route.UpdatePasswordRoute

fun NavGraphBuilder.loginNavigation(
	onNavigateToSignIn: () -> Unit,
	onNavigateToSummary: () -> Unit,
	onNavigateToBrowser: (title: String, url: String) -> Unit,
	onDismissRequest: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	navigation<LoginDestination.NavGraph>(startDestination = LoginDestination.SignIn) {
		composable<LoginDestination.SignIn> {
			SignInRoute(
				onNavigateToSummary = onNavigateToSummary,
				onNavigateToBrowser = onNavigateToBrowser,
				showSnackBar = showSnackBar
			)
		}

		dialog<LoginDestination.SignOutDialog> {
			SignOutRoute(
				onNavigateToSignIn = onNavigateToSignIn,
				onDismissRequest = onDismissRequest,
				showSnackBar = showSnackBar
			)
		}

		dialog<LoginDestination.UpdatePasswordDialog> {
			UpdatePasswordRoute(
				onDismissRequest = onDismissRequest,
				showSnackBar = showSnackBar
			)
		}
	}
}