package com.gdavidpb.tuindice.login.presentation.navigation

import androidx.compose.material.navigation.bottomSheet
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.login.presentation.route.UpdatePasswordRoute

fun NavController.navigateToUpdatePassword() {
	navigate(Destination.UpdatePassword.route)
}

fun NavGraphBuilder.updatePasswordDialog(
	onDismissRequest: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	bottomSheet(Destination.UpdatePassword.route) {
		UpdatePasswordRoute(
			onDismissRequest = onDismissRequest,
			showSnackBar = showSnackBar
		)
	}
}