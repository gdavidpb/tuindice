package com.gdavidpb.tuindice.login.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gdavidpb.tuindice.base.presentation.navigation.BrowserArgs
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.login.presentation.route.SignInRoute

fun NavController.navigateToSignIn() {
	navigate(Destination.SignIn.route)
}

fun NavGraphBuilder.signInScreen(
	navigateToSummary: () -> Unit,
	navigateToBrowser: (args: BrowserArgs) -> Unit,
	showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit
) {
	composable(Destination.SignIn.route) {
		SignInRoute(
			onNavigateToSummary = navigateToSummary,
			onNavigateToBrowser = { title, url ->
				navigateToBrowser(
					BrowserArgs(
						title = title,
						url = url
					)
				)
			},
			showSnackBar = showSnackBar
		)
	}
}