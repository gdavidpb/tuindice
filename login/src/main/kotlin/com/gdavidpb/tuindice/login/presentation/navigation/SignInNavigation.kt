package com.gdavidpb.tuindice.login.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.utils.extension.navigatePopUpTo
import com.gdavidpb.tuindice.login.presentation.route.SignInRoute

fun NavController.navigateToSignIn() {
	navigatePopUpTo(Destination.SignIn)
}

fun NavGraphBuilder.signInScreen(
	navigateToSummary: () -> Unit,
	navigateToBrowser: (title: String, url: String) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	composable(Destination.SignIn.route) {
		SignInRoute(
			onNavigateToSummary = navigateToSummary,
			onNavigateToBrowser = navigateToBrowser,
			showSnackBar = showSnackBar
		)
	}
}