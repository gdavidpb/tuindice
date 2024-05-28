package com.gdavidpb.tuindice.login.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.login.presentation.route.UpdatePasswordRoute

fun NavController.navigateToUpdatePassword() {
	navigate(Destination.UpdatePassword)
}

fun NavGraphBuilder.updatePasswordDialog(
	onDismissRequest: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	dialog<Destination.UpdatePassword> {
		UpdatePasswordRoute(
			onDismissRequest = onDismissRequest,
			showSnackBar = showSnackBar
		)
	}
}