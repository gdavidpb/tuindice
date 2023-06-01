package com.gdavidpb.tuindice.enrollmentproof.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.enrollmentproof.presentation.route.EnrollmentProofFetchRoute

fun NavController.navigateToEnrollmentProofFetch() {
	navigate(Destination.EnrollmentProofFetch.route)
}

fun NavGraphBuilder.enrollmentProofFetchDialog(
	navigateToUpdatePassword: () -> Unit,
	onDismissRequest: () -> Unit,
	showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit
) {
	dialog(Destination.EnrollmentProofFetch.route) {
		EnrollmentProofFetchRoute(
			onNavigateToUpdatePassword = navigateToUpdatePassword,
			onDismissRequest = onDismissRequest,
			showSnackBar = showSnackBar
		)
	}
}