package com.gdavidpb.tuindice.login.presentation.navigation

import androidx.compose.material.navigation.bottomSheet
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.login.presentation.route.SignOutRoute

fun NavController.navigateToSignOut() {
	navigate(Destination.SignOut.route)
}

fun NavGraphBuilder.signOutDialog(
	navigateToSignIn: () -> Unit,
	onDismissRequest: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	bottomSheet(Destination.SignOut.route) {
		SignOutRoute(
			onNavigateToSignIn = navigateToSignIn,
			onDismissRequest = onDismissRequest,
			showSnackBar = showSnackBar
		)
	}
}