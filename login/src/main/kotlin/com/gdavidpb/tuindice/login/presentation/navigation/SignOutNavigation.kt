package com.gdavidpb.tuindice.login.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.login.presentation.route.SignOutRoute

fun NavController.navigateToSignOut() {
	navigate(Destination.SignOut)
}

fun NavGraphBuilder.signOutDialog(
	navigateToSignIn: () -> Unit,
	onDismissRequest: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	dialog<Destination.SignOut> {
		SignOutRoute(
			onNavigateToSignIn = navigateToSignIn,
			onDismissRequest = onDismissRequest,
			showSnackBar = showSnackBar
		)
	}
}