package com.gdavidpb.tuindice.enrollmentproof.presentation.navigation

import androidx.compose.material.navigation.bottomSheet
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.enrollmentproof.presentation.route.EnrollmentProofFetchRoute

fun NavController.navigateToEnrollmentProofFetch() {
	navigate(Destination.EnrollmentProofFetch.route)
}

fun NavGraphBuilder.enrollmentProofFetchDialog(
	navigateToUpdatePassword: () -> Unit,
	onDismissRequest: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	bottomSheet(Destination.EnrollmentProofFetch.route) {
		EnrollmentProofFetchRoute(
			onNavigateToUpdatePassword = navigateToUpdatePassword,
			onDismissRequest = onDismissRequest,
			showSnackBar = showSnackBar
		)
	}
}